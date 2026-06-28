-- ============================================================
-- V4: Tabelas de sessões de treino
--
-- workout_sessions: representa uma execução de treino em andamento ou concluída
-- session_sets: cada série que o usuário registrou durante a sessão (performance real)
--
-- Decisões de design:
--   - TREINO VIVO: workout_id é FK direta para workouts (sem snapshot).
--     Se o treino for editado, a sessão em andamento reflete o novo estado.
--     Porém, session_sets.exercise_id é FK direta para exercises —
--     o histórico de o que foi feito é preservado mesmo que o treino mude.
--
--   - completed_at NULL → sessão em andamento
--   - completed_at NOT NULL → sessão concluída
--
--   - O usuário CRIA sets dinamicamente durante a execução (não há sets pré-gerados).
--     set_number começa em 1 e é único por (session_id, exercise_id).
--
--   - weight_kg é NULLABLE: nem todo exercício usa peso (ex: prancha, flexão)
--   - rest_seconds é NULLABLE: o usuário pode não registrar o descanso
--
--   - ON DELETE RESTRICT em workout_id: não pode deletar um treino com sessões ativas
--   - ON DELETE CASCADE em session_id: deletar sessão apaga todos os sets dela
-- ============================================================

CREATE TABLE workout_sessions (
    id           BIGSERIAL                NOT NULL,
    workout_id   BIGINT                   NOT NULL,
    user_id      BIGINT                   NOT NULL,
    started_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    notes        TEXT,

    CONSTRAINT pk_workout_sessions         PRIMARY KEY (id),
    CONSTRAINT fk_workout_sessions_workout FOREIGN KEY (workout_id)
                                               REFERENCES workouts (id)
                                               ON DELETE RESTRICT,
    CONSTRAINT fk_workout_sessions_user    FOREIGN KEY (user_id)
                                               REFERENCES users (id)
                                               ON DELETE CASCADE
);

-- Índice para listar o histórico de sessões de um usuário (GET /api/v1/sessions/history)
CREATE INDEX idx_workout_sessions_user_id    ON workout_sessions (user_id);

-- Índice para busca por data (filtros de histórico por período)
CREATE INDEX idx_workout_sessions_started_at ON workout_sessions (started_at DESC);

-- Índice para listar sessões de um treino específico
CREATE INDEX idx_workout_sessions_workout_id ON workout_sessions (workout_id);

-- ----

CREATE TABLE session_sets (
    id           BIGSERIAL                NOT NULL,
    session_id   BIGINT                   NOT NULL,
    exercise_id  BIGINT                   NOT NULL,
    set_number   INTEGER                  NOT NULL,
    reps_done    INTEGER                  NOT NULL,
    weight_kg    NUMERIC(6,2),
    rest_seconds INTEGER,
    notes        TEXT,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_session_sets           PRIMARY KEY (id),
    CONSTRAINT fk_session_sets_session   FOREIGN KEY (session_id)
                                             REFERENCES workout_sessions (id)
                                             ON DELETE CASCADE,
    CONSTRAINT fk_session_sets_exercise  FOREIGN KEY (exercise_id)
                                             REFERENCES exercises (id)
                                             ON DELETE RESTRICT,

    -- Não pode haver dois "set nº 1" do mesmo exercício na mesma sessão
    CONSTRAINT uq_session_set_number     UNIQUE (session_id, exercise_id, set_number),

    CONSTRAINT chk_reps_positive         CHECK (reps_done > 0),
    CONSTRAINT chk_weight_positive       CHECK (weight_kg IS NULL OR weight_kg >= 0),
    CONSTRAINT chk_rest_positive         CHECK (rest_seconds IS NULL OR rest_seconds >= 0),
    CONSTRAINT chk_set_number_positive   CHECK (set_number > 0)
);

-- Índice para carregar todos os sets de uma sessão
CREATE INDEX idx_session_sets_session_id  ON session_sets (session_id);

-- Índice para calcular métricas por exercício (evolução de carga, volume)
CREATE INDEX idx_session_sets_exercise_id ON session_sets (exercise_id);
