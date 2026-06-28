-- ============================================================
-- V5: Exercícios iniciais do sistema
--
-- Todos com is_system = TRUE e created_by = NULL.
-- Visíveis para todos os usuários. Não podem ser editados ou deletados via API.
-- Cada grupo muscular tem entre 4 e 6 exercícios representativos.
-- ============================================================

INSERT INTO exercises (name, muscle_group, description, is_system, created_by) VALUES

-- PEITO (CHEST)
('Supino Reto com Barra',       'CHEST', 'Exercício básico de peito com barra em banco plano. Foco no peitoral médio.', TRUE, NULL),
('Supino Inclinado com Halteres','CHEST', 'Supino em banco inclinado com halteres. Ênfase no peitoral superior.', TRUE, NULL),
('Crucifixo com Halteres',      'CHEST', 'Movimento de abertura em banco plano. Isola o peitoral e trabalha a amplitude.', TRUE, NULL),
('Flexão de Braço',             'CHEST', 'Exercício com peso corporal. Trabalha peito, tríceps e ombros simultaneamente.', TRUE, NULL),
('Peck Deck (Voador)',          'CHEST', 'Máquina de adução do ombro. Isolamento do peitoral com menor risco de lesão.', TRUE, NULL),

-- COSTAS (BACK)
('Puxada Frontal no Pulley',    'BACK', 'Puxada com barra reta no aparelho. Trabalha grande dorsal e bíceps.', TRUE, NULL),
('Remada Curvada com Barra',    'BACK', 'Remada inclinada com barra. Trabalha toda a musculatura posterior das costas.', TRUE, NULL),
('Levantamento Terra',          'BACK', 'Exercício composto fundamental. Trabalha lombar, glúteos, posterior de coxa e costas.', TRUE, NULL),
('Barra Fixa (Pull-up)',        'BACK', 'Exercício com peso corporal. Trabalha grande dorsal, bíceps e core.', TRUE, NULL),
('Remada Unilateral com Haltere','BACK', 'Remada com apoio no banco, um lado de cada vez. Foco em dorsais e romboides.', TRUE, NULL),

-- PERNAS (LEGS)
('Agachamento Livre',           'LEGS', 'Exercício composto fundamental. Trabalha quadríceps, glúteos e posterior de coxa.', TRUE, NULL),
('Leg Press 45°',               'LEGS', 'Agachamento na máquina inclinada. Permite maior carga com menos stress na lombar.', TRUE, NULL),
('Cadeira Extensora',           'LEGS', 'Isolamento de quadríceps na máquina. Complemento ao agachamento.', TRUE, NULL),
('Mesa Flexora',                'LEGS', 'Isolamento do posterior de coxa na máquina. Auxílio ao levantamento terra.', TRUE, NULL),
('Afundo (Lunge)',              'LEGS', 'Exercício unilateral. Trabalha quadríceps, glúteos e equilíbrio.', TRUE, NULL),
('Panturrilha em Pé',          'LEGS', 'Isolamento de gastrocnêmio e sóleo. Pode ser feito com ou sem carga.', TRUE, NULL),

-- OMBROS (SHOULDERS)
('Desenvolvimento com Halteres','SHOULDERS', 'Press overhead com halteres. Trabalha deltoide anterior, médio e tríceps.', TRUE, NULL),
('Elevação Lateral com Halteres','SHOULDERS', 'Isolamento do deltoide médio. Fundamental para largura dos ombros.', TRUE, NULL),
('Remada Alta com Barra',       'SHOULDERS', 'Puxada vertical ao queixo. Trabalha trapézio e deltoide médio.', TRUE, NULL),
('Elevação Frontal com Halteres','SHOULDERS', 'Isolamento do deltoide anterior. Complemento ao desenvolvimento.', TRUE, NULL),

-- BRAÇOS (ARMS)
('Rosca Direta com Barra',      'ARMS', 'Flexão de cotovelo com barra. Exercício básico de bíceps.', TRUE, NULL),
('Rosca Martelo com Halteres',  'ARMS', 'Flexão neutra de cotovelo. Trabalha braquial e braquiorradial além do bíceps.', TRUE, NULL),
('Tríceps Pulley (Corda)',      'ARMS', 'Extensão de cotovelo no cabo. Isolamento dos três feixes do tríceps.', TRUE, NULL),
('Tríceps Testa com Barra W',   'ARMS', 'Extensão de cotovelo em banco plano. Foco no feixe longo do tríceps.', TRUE, NULL),
('Rosca Concentrada',           'ARMS', 'Flexão de cotovelo com apoio no joelho. Máximo isolamento do bíceps.', TRUE, NULL),

-- CORE (ABDÔMEN)
('Prancha Frontal',             'CORE', 'Isometria de core. Trabalha todos os músculos abdominais e estabilizadores.', TRUE, NULL),
('Abdominal Crunch',            'CORE', 'Flexão de tronco básica. Foco no reto abdominal.', TRUE, NULL),
('Elevação de Pernas Suspenso', 'CORE', 'Flexão de quadril com pernas retas. Trabalha reto abdominal inferior e psoas.', TRUE, NULL),
('Abdominal Oblíquo (Bicicleta)','CORE', 'Rotação de tronco com joelho alternado. Trabalha oblíquos e reto abdominal.', TRUE, NULL),

-- GLÚTEOS (GLUTES)
('Hip Thrust com Barra',        'GLUTES', 'Extensão de quadril com barra sobre os quadris. Melhor exercício para isolamento dos glúteos.', TRUE, NULL),
('Elevação Pélvica no Chão',    'GLUTES', 'Versão com peso corporal do hip thrust. Ótimo para iniciantes.', TRUE, NULL),
('Abdução de Quadril na Máquina','GLUTES', 'Abertura de pernas na máquina. Trabalha glúteo médio e mínimo.', TRUE, NULL),
('Agachamento Sumô',            'GLUTES', 'Agachamento com pés afastados e pontas dos pés para fora. Maior ativação do glúteo.', TRUE, NULL);
