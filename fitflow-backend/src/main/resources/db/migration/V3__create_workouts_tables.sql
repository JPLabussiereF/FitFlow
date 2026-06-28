-- ============================================================
-- V3: Tabelas de treinos
--
-- workouts: representa um plano de treino com lista de exercícios
-- workout_exercises: tabela de junção — quais exercícios compõem um treino,
--                   com valores SUGERIDOS (planejamento), não realizados
--
-- Decisões de design:
--   - is_template = true + user_id NULL → template do sistema (read-only via API)
--   - is_template = false + user_id NOT NULL → treino do usuário
--   - Usuário pode CLONAR um template → cria cópia com is_template=false e seu user_id
--   - suggested_* são opcionais — o usuário registra o que REALMENTE faz na session_sets
--   - display_order controla a ordem dos exercícios na "playlist" do treino
--   - Mesmo exercício pode aparecer mais de uma vez no treino (sem UNIQUE em exercise_id)
-- ============================================================

CREATE TABLE workouts (
    id          BIGSERIAL                NOT NULL,
    user_id     BIGINT,
    name        VARCHAR(100)             NOT NULL,
    type        VARCHAR(10)              NOT NULL DEFAULT 'A',
    is_template BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_workouts          PRIMARY KEY (id),
    CONSTRAINT fk_workouts_user     FOREIGN KEY (user_id)
                                        REFERENCES users (id)
                                        ON DELETE CASCADE,

    -- Templates do sistema não têm dono; treinos de usuário devem ter dono
    CONSTRAINT chk_workouts_owner   CHECK (
        (is_template = TRUE  AND user_id IS NULL) OR
        (is_template = FALSE AND user_id IS NOT NULL)
    )
);

-- Índice para listar treinos de um usuário (GET /api/v1/workouts)
CREATE INDEX idx_workouts_user_id ON workouts (user_id);

-- ----

CREATE TABLE workout_exercises (
    id                      BIGSERIAL     NOT NULL,
    workout_id              BIGINT        NOT NULL,
    exercise_id             BIGINT        NOT NULL,
    display_order           INTEGER       NOT NULL DEFAULT 0,

    -- Valores sugeridos/planejados — todos opcionais.
    -- O que o usuário REALMENTE fez fica em session_sets.
    suggested_sets          INTEGER,
    suggested_reps          INTEGER,
    suggested_weight_kg     NUMERIC(6,2),
    suggested_rest_seconds  INTEGER,
    notes                   TEXT,

    CONSTRAINT pk_workout_exercises          PRIMARY KEY (id),
    CONSTRAINT fk_workout_exercises_workout  FOREIGN KEY (workout_id)
                                                 REFERENCES workouts (id)
                                                 ON DELETE CASCADE,
    CONSTRAINT fk_workout_exercises_exercise FOREIGN KEY (exercise_id)
                                                 REFERENCES exercises (id)
                                                 ON DELETE RESTRICT,

    -- Dois exercícios não podem ocupar a mesma posição na playlist do mesmo treino
    CONSTRAINT uq_workout_exercise_order     UNIQUE (workout_id, display_order)
);

-- Índice para carregar todos os exercícios de um treino
CREATE INDEX idx_workout_exercises_workout_id  ON workout_exercises (workout_id);

-- Índice para encontrar em quais treinos um exercício aparece
CREATE INDEX idx_workout_exercises_exercise_id ON workout_exercises (exercise_id);
