-- ============================================================
-- V2: Tabela de exercícios
--
-- Decisões de design:
--   - is_system = true  → exercício global pré-cadastrado (readonly via API)
--   - is_system = false → exercício criado pelo usuário (created_by NOT NULL)
--   - created_by NULL   → exclusivo de exercícios de sistema
--   - image_url é opcional; frontend exibe ícone do grupo muscular como fallback
--   - ON DELETE SET NULL: se o usuário for deletado, seus exercícios ficam "órfãos"
--     mas permanecem no banco (protegem histórico de sessões que os referenciam)
-- ============================================================

-- Enum de grupos musculares.
-- Usar CREATE TYPE garante integridade em nível de banco.
-- Para adicionar novos valores no futuro: ALTER TYPE muscle_group ADD VALUE 'CALVES';
CREATE TYPE muscle_group AS ENUM (
    'CHEST',
    'BACK',
    'LEGS',
    'SHOULDERS',
    'ARMS',
    'CORE',
    'GLUTES'
);

CREATE TABLE exercises (
    id           BIGSERIAL                NOT NULL,
    name         VARCHAR(100)             NOT NULL,
    muscle_group muscle_group             NOT NULL,
    description  TEXT,
    image_url    VARCHAR(500),
    is_system    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_by   BIGINT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_exercises         PRIMARY KEY (id),
    CONSTRAINT fk_exercises_user    FOREIGN KEY (created_by)
                                        REFERENCES users (id)
                                        ON DELETE SET NULL,

    -- Exercícios de sistema não têm dono; exercícios de usuário devem ter dono
    CONSTRAINT chk_exercises_owner  CHECK (
        (is_system = TRUE  AND created_by IS NULL) OR
        (is_system = FALSE AND created_by IS NOT NULL)
    )
);

-- Índice para busca por grupo muscular (rota GET /api/v1/exercises?muscleGroup=...)
CREATE INDEX idx_exercises_muscle_group ON exercises (muscle_group);

-- Índice para listar exercícios de um usuário específico
CREATE INDEX idx_exercises_created_by ON exercises (created_by);
