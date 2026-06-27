# 🗺️ FitFlow — Roadmap de Desenvolvimento

> Este arquivo é a **fonte da verdade** do progresso do projeto.
> Cada etapa é marcada como `[X]` quando concluída e validada com testes.

---

## Legenda

- `[ ]` Não iniciado
- `[~]` Em progresso
- `[X]` Concluído e testado

---

## 🟣 Fase 1 — Fundação

### 1.1 Repositório e Ambiente
- [X] Criar repositório no GitHub e configurar `.gitignore`
- [X] Configurar projeto Spring Boot com todas as dependências (`pom.xml`)
- [X] Configurar Docker Compose com PostgreSQL 16
- [X] Criar `ROADMAP.md`, `CLAUDE.md` e pipeline CI (`ci.yml`)

### 1.2 Migrations com Flyway
- [X] Criar `V1__create_users_table.sql` — tabela `users`
- [ ] Criar `V2__create_exercises_table.sql` — tabela `exercises`
- [ ] Criar `V3__create_workouts_table.sql` — tabelas `workouts` e `workout_exercises`
- [ ] Criar `V4__create_sessions_tables.sql` — tabelas `workout_sessions` e `session_sets`
- [ ] Criar `V5__seed_exercises.sql` — popular tabela com exercícios iniciais
- [ ] Validar que o Flyway roda todas as migrations sem erro ao subir a aplicação

### 1.3 Autenticação com JWT
- [ ] Criar entidade `User` com campos: `id`, `name`, `email`, `password`, `createdAt` (`@CreationTimestamp` — imutável), `updatedAt` (`@UpdateTimestamp` — atualizado automaticamente pelo Hibernate)
- [ ] Criar `UserRepository` (interface JPA)
- [ ] Criar `UserService` com método `loadUserByUsername` (Spring Security)
- [ ] Implementar `JwtService`: geração, validação e extração de claims do token
- [ ] Implementar `JwtAuthenticationFilter`: intercepta requests e valida o token
- [ ] Criar `SecurityConfig`: define rotas públicas vs. protegidas, desabilita CSRF, configura stateless
- [ ] Criar `AuthController` com dois endpoints:
  - `POST /api/auth/register` — cadastra usuário com senha hasheada (BCrypt)
  - `POST /api/auth/login` — valida credenciais e retorna o JWT
- [ ] Criar DTOs: `RegisterRequestDTO`, `LoginRequestDTO`, `AuthResponseDTO`
- [ ] **Testes:**
  - `JwtServiceTest` — testa geração e validação do token (JUnit 5)
  - `AuthControllerTest` — testa register e login com MockMvc (cenários: sucesso, email duplicado, senha errada)
  - `UserServiceTest` — testa `loadUserByUsername` com Mockito
- [ ] Testar endpoints manualmente com Postman/Insomnia

---

## 🟣 Fase 2 — Catálogo de Exercícios

### 2.1 Back-end
- [ ] Criar entidade `Exercise` com campos: `id`, `name`, `muscleGroup`, `description`, `imageUrl`
- [ ] Criar enum `MuscleGroup` (CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, GLUTES)
- [ ] Criar `ExerciseRepository` com query customizada para filtrar por `muscleGroup`
- [ ] Criar `ExerciseService` com métodos: `findAll()`, `findByMuscleGroup()`
- [ ] Criar `ExerciseController`:
  - `GET /api/exercises` — lista todos (com parâmetro opcional `?muscleGroup=CHEST`)
- [ ] Criar `ExerciseResponseDTO` e `ExerciseMapper` (MapStruct)
- [ ] **Testes:**
  - `ExerciseServiceTest` — testa listagem e filtro por grupo muscular (Mockito)
  - `ExerciseControllerTest` — testa GET com e sem filtro (MockMvc)

### 2.2 Front-end (Angular)
- [ ] Criar projeto Angular com Angular Material e tema roxo customizado
- [ ] Criar `ExerciseService` (HTTP GET para `/api/exercises`)
- [ ] Criar componente `ExerciseListComponent` com cards por grupo muscular
- [ ] Criar `ExerciseFilterComponent` (chips/select por grupo muscular)
- [ ] Configurar `HttpClientModule` e `environment.ts` com a URL da API
- [ ] **Testes:**
  - `ExerciseService.spec.ts` — testa chamada HTTP com `HttpClientTestingModule`
  - `ExerciseListComponent.spec.ts` — testa renderização dos cards

---

## 🟣 Fase 3 — Criação de Treinos

### 3.1 Back-end
- [ ] Criar entidade `Workout` com campos: `id`, `user`, `name`, `type` (A/B/C), `createdAt`
- [ ] Criar entidade `WorkoutExercise` (tabela de junção): `workout`, `exercise`, `sets`, `reps`, `weightKg`, `restSeconds`, `order`
- [ ] Criar `WorkoutRepository` e `WorkoutExerciseRepository`
- [ ] Criar `WorkoutService`: `create()`, `findAllByUser()`, `findById()`, `update()`, `delete()`
- [ ] Criar `WorkoutController` (rotas protegidas por JWT):
  - `POST /api/workouts`
  - `GET /api/workouts`
  - `GET /api/workouts/{id}`
  - `PUT /api/workouts/{id}`
  - `DELETE /api/workouts/{id}`
- [ ] Criar DTOs e Mappers para `Workout` e `WorkoutExercise`
- [ ] **Testes:**
  - `WorkoutServiceTest` — testa CRUD completo com Mockito
  - `WorkoutControllerTest` — testa endpoints com MockMvc + autenticação simulada

### 3.2 Front-end (Angular)
- [ ] Criar `WorkoutService` (CRUD HTTP)
- [ ] Criar `WorkoutListComponent` — lista os treinos do usuário
- [ ] Criar `WorkoutFormComponent` — formulário de criação/edição com drag-and-drop de exercícios
- [ ] Integrar `@angular/cdk/drag-drop` para ordenar exercícios na "playlist"
- [ ] **Testes:**
  - `WorkoutService.spec.ts`
  - `WorkoutFormComponent.spec.ts` — testa adição e remoção de exercícios

---

## 🟣 Fase 4 — Execução do Treino

### 4.1 Back-end
- [ ] Criar entidade `WorkoutSession`: `id`, `workout`, `startedAt`, `completedAt`, `notes`
- [ ] Criar entidade `SessionSet`: `id`, `session`, `exercise`, `sets`, `reps`, `weightKg`, `restSeconds`, `notes`, `completed`
- [ ] Criar `WorkoutSessionService`:
  - `startSession(workoutId)` — cria sessão e copia os exercícios do treino
  - `completeSession(sessionId)` — registra `completedAt`
  - `updateSet(sessionId, setId, data)` — atualiza um set específico
- [ ] Criar `WorkoutSessionController`:
  - `POST /api/sessions` — inicia sessão
  - `PATCH /api/sessions/{id}/complete` — conclui sessão
  - `PATCH /api/sessions/{id}/sets/{setId}` — atualiza um set
- [ ] **Testes:**
  - `WorkoutSessionServiceTest` — testa início, atualização e conclusão de sessão
  - `WorkoutSessionControllerTest` — testa os três endpoints

### 4.2 Front-end (Angular)
- [ ] Criar `SessionService` (HTTP)
- [ ] Criar `WorkoutExecutionComponent` — tela de execução com timer de descanso
- [ ] Checkbox por exercício com atualização em tempo real via PATCH
- [ ] Botão "Concluir Treino" com confirmação
- [ ] **Testes:**
  - `SessionService.spec.ts`
  - `WorkoutExecutionComponent.spec.ts` — testa check de exercícios e conclusão

---

## 🟣 Fase 5 — Histórico e Métricas

### 5.1 Back-end
- [ ] Endpoint `GET /api/sessions/history` — últimas sessões (paginado, filtro por data)
- [ ] Endpoint `GET /api/metrics/summary` — volume total, frequência e evolução de carga por exercício
- [ ] Criar `@Scheduled` job para compactar sessões com mais de 30 dias em métricas agregadas
- [ ] **Testes:**
  - `MetricsServiceTest` — testa cálculos de agregação
  - `HistoryControllerTest` — testa paginação e filtros

### 5.2 Front-end (Angular)
- [ ] Criar `DashboardComponent` com Chart.js (frequência semanal, evolução de carga)
- [ ] Criar `HistoryComponent` com lista paginada de sessões passadas
- [ ] Implementar exportação de relatório em PDF (biblioteca `jsPDF`)
- [ ] **Testes:**
  - `DashboardComponent.spec.ts` — testa renderização dos gráficos

---

## 🏁 Deploy (Pós-MVP)

- [ ] Configurar variáveis de ambiente de produção (JWT secret, DB URL)
- [ ] Deploy do back-end no Railway ou Render
- [ ] Deploy do front-end na Vercel ou Netlify
- [ ] Configurar GitHub Actions para deploy automático após merge na `main`