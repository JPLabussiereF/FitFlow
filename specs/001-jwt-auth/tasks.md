# Tasks: Autenticação de Usuário com JWT

**Input**: Design documents from `/specs/001-jwt-auth/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Tests**: OBRIGATÓRIOS — Princípio I da Constituição (Test-First NON-NEGOTIABLE).
Testes de serviços devem ser escritos ANTES da implementação (TDD).

**Organization**: Tarefas agrupadas por user story para entrega incremental independente.

---

## Formato: `[ID] [P?] [Story?] Descrição + caminho do arquivo`

- **[P]**: pode ser executada em paralelo (arquivos diferentes, sem dependências incompletas)
- **[US1/2/3]**: a qual user story da spec.md esta tarefa pertence
- Caminhos relativos à raiz do repositório

---

## Phase 1: Setup

**Purpose**: Configurar a aplicação para que o JWT funcione via variáveis de ambiente.

- [ ] T001 Confirmar que `V1__create_users_table.sql` possui os campos `id, name, email, password, created_at, updated_at` com as constraints corretas em `fitflow-backend/src/main/resources/db/migration/V1__create_users_table.sql`
- [ ] T002 Adicionar propriedades JWT no `fitflow-backend/src/main/resources/application.yml`: `jwt.secret` (via `${JWT_SECRET}`) e `jwt.expiration` (86400000 ms = 24h), e habilitar `spring.mvc.problemdetails.enabled: true`

**Checkpoint**: Configuração base pronta — application.yml com propriedades JWT e ProblemDetail habilitado.

---

## Phase 2: Fundação (bloqueia todas as User Stories)

**Purpose**: Entidade, repositório, DTOs, infraestrutura de segurança e serviços
compartilhados. NENHUMA user story pode começar sem esta fase concluída.

**⚠️ CRÍTICO**: Siga a ordem das tarefas — dependências estão sequenciadas.

### Entidade e Repositório

- [ ] T003 [P] Criar `User.java` implementando `UserDetails` com campos `id, name, email, password, createdAt, updatedAt`, anotações Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor` e `@Entity @Table(name="users")` em `fitflow-backend/src/main/java/com/fitflow/model/User.java`
- [ ] T004 Criar `UserRepository.java` extendendo `JpaRepository<User, Long>` com método `findByEmail(String email): Optional<User>` em `fitflow-backend/src/main/java/com/fitflow/repository/UserRepository.java`

### DTOs

- [ ] T005 [P] Criar `RegisterRequestDTO.java` com campos `name (@NotBlank @Size(max=100))`, `email (@NotBlank @Email)`, `password (@NotBlank @Size(min=6, max=72))` em `fitflow-backend/src/main/java/com/fitflow/dto/auth/RegisterRequestDTO.java`
- [ ] T006 [P] Criar `LoginRequestDTO.java` com campos `email (@NotBlank @Email)`, `password (@NotBlank)` em `fitflow-backend/src/main/java/com/fitflow/dto/auth/LoginRequestDTO.java`
- [ ] T007 [P] Criar `AuthResponseDTO.java` com campos `token, userId, name, email` em `fitflow-backend/src/main/java/com/fitflow/dto/auth/AuthResponseDTO.java`

### Exceções e Tratamento de Erros

- [ ] T008 [P] Criar `EmailAlreadyExistsException.java` (RuntimeException) em `fitflow-backend/src/main/java/com/fitflow/exception/EmailAlreadyExistsException.java`
- [ ] T009 Criar `GlobalExceptionHandler.java` com `@RestControllerAdvice` tratando: `EmailAlreadyExistsException → 409`, `BadCredentialsException → 401 "Credenciais inválidas"`, `MethodArgumentNotValidException → 400` — todos retornando `ProblemDetail` em `fitflow-backend/src/main/java/com/fitflow/exception/GlobalExceptionHandler.java`

### Serviço JWT (TDD)

- [ ] T010 [P] Escrever `JwtServiceTest.java` com os 5 cenários: `generateToken_shouldReturnNonNull`, `extractUsername_shouldReturnEmail`, `validateToken_shouldReturnTrueForValid`, `validateToken_shouldReturnFalseForExpired`, `validateToken_shouldReturnFalseForWrongUser` em `fitflow-backend/src/test/java/com/fitflow/service/JwtServiceTest.java`
- [ ] T011 Implementar `JwtService.java` com métodos `generateToken(UserDetails)`, `validateToken(String, UserDetails)`, `extractUsername(String)` usando JJWT 0.12.x e `@Value("${jwt.secret}")` em `fitflow-backend/src/main/java/com/fitflow/service/JwtService.java`

### Serviço de Usuário (TDD)

- [ ] T012 [P] Escrever `UserServiceTest.java` com os 2 cenários: `loadUserByUsername_shouldReturnUserDetails` e `loadUserByUsername_shouldThrowUsernameNotFoundException` em `fitflow-backend/src/test/java/com/fitflow/service/UserServiceTest.java`
- [ ] T013 Implementar `UserService.java` implementando `UserDetailsService` com `loadUserByUsername(String email)` em `fitflow-backend/src/main/java/com/fitflow/service/UserService.java`

### Configuração de Segurança

- [ ] T014 Criar `ApplicationConfig.java` com `@Bean` para `PasswordEncoder` (BCrypt), `AuthenticationProvider` (DaoAuthenticationProvider) e `AuthenticationManager` em `fitflow-backend/src/main/java/com/fitflow/config/ApplicationConfig.java`
- [ ] T015 Criar `SecurityConfig.java` com `@Bean SecurityFilterChain`: CSRF desabilitado, sessão `STATELESS`, rotas públicas (`/api/v1/auth/**`), demais rotas autenticadas, e `JwtAuthenticationFilter` antes do `UsernamePasswordAuthenticationFilter` em `fitflow-backend/src/main/java/com/fitflow/config/SecurityConfig.java`
- [ ] T016 Criar `JwtAuthenticationFilter.java` extendendo `OncePerRequestFilter`: extrai token do header `Authorization: Bearer`, valida com `JwtService`, seta `UsernamePasswordAuthenticationToken` no `SecurityContextHolder` em `fitflow-backend/src/main/java/com/fitflow/security/JwtAuthenticationFilter.java`

**Checkpoint**: Fundação completa — entidade, repositório, DTOs, JWT e segurança configurados. Verificar que a aplicação sobe sem erros (`./mvnw spring-boot:run`).

---

## Phase 3: User Story 1 — Cadastro de Novo Usuário (Priority: P1) 🎯 MVP

**Goal**: Usuário consegue se registrar e receber um JWT imediatamente após o cadastro.

**Independent Test**: `POST /api/v1/auth/register` com dados válidos retorna 201 + token;
com e-mail duplicado retorna 409; com dados inválidos retorna 400.

### Testes — US1 ⚠️ ESCREVER ANTES DA IMPLEMENTAÇÃO

- [ ] T017 [P] [US1] Escrever `AuthServiceTest.java` com 4 cenários de `register()`: `shouldHashPassword`, `shouldThrowOnDuplicateEmail`, `shouldReturnAuthResponse`, `shouldNormalizeEmail` — todos com Mockito em `fitflow-backend/src/test/java/com/fitflow/service/AuthServiceTest.java`
- [ ] T018 [P] [US1] Escrever cenários de `register()` em `AuthControllerTest.java` com MockMvc: 201 com dados válidos, 409 e-mail duplicado, 400 e-mail inválido, 400 senha curta, 400 nome vazio em `fitflow-backend/src/test/java/com/fitflow/controller/AuthControllerTest.java`

### Implementação — US1

- [ ] T019 [US1] Implementar `AuthService.java` com método `register(RegisterRequestDTO)`: normalizar email, verificar duplicata, hash BCrypt, salvar User, gerar JWT via `JwtService`, retornar `AuthResponseDTO` em `fitflow-backend/src/main/java/com/fitflow/service/AuthService.java`
- [ ] T020 [US1] Implementar `AuthController.java` com endpoint `POST /api/v1/auth/register` delegando para `AuthService.register()` e retornando 201 Created em `fitflow-backend/src/main/java/com/fitflow/controller/AuthController.java`

**Checkpoint**: User Story 1 independentemente funcional. Rodar `./mvnw test -Dtest=AuthServiceTest,AuthControllerTest` — todos devem passar. Testar manualmente os steps 4 e 5 do `quickstart.md`.

---

## Phase 4: User Story 2 — Login de Usuário Existente (Priority: P2)

**Goal**: Usuário já cadastrado consegue autenticar e receber um JWT válido.

**Independent Test**: `POST /api/v1/auth/login` com credenciais corretas retorna 200 + token;
com senha errada ou e-mail inexistente retorna 401 com mensagem genérica.

### Testes — US2 ⚠️ ESCREVER ANTES DA IMPLEMENTAÇÃO

- [ ] T021 [P] [US2] Adicionar cenários de `login()` ao `AuthServiceTest.java`: `shouldReturnAuthResponseForValidCredentials`, `shouldThrowBadCredentialsForWrongPassword`, `shouldNormalizeEmailOnLogin` em `fitflow-backend/src/test/java/com/fitflow/service/AuthServiceTest.java`
- [ ] T022 [P] [US2] Adicionar cenários de `login()` ao `AuthControllerTest.java`: 200 com credenciais válidas, 401 senha errada, 401 e-mail inexistente em `fitflow-backend/src/test/java/com/fitflow/controller/AuthControllerTest.java`

### Implementação — US2

- [ ] T023 [US2] Adicionar método `login(LoginRequestDTO)` ao `AuthService.java`: normalizar email, autenticar via `AuthenticationManager`, carregar User, gerar JWT, retornar `AuthResponseDTO` em `fitflow-backend/src/main/java/com/fitflow/service/AuthService.java`
- [ ] T024 [US2] Adicionar endpoint `POST /api/v1/auth/login` ao `AuthController.java` delegando para `AuthService.login()` e retornando 200 OK em `fitflow-backend/src/main/java/com/fitflow/controller/AuthController.java`

**Checkpoint**: User Story 2 independentemente funcional. Rodar todos os testes — devem passar. Testar manualmente o step 6 do `quickstart.md`.

---

## Phase 5: User Story 3 — Acesso a Rotas Protegidas com Token (Priority: P3)

**Goal**: Requisições com token válido passam pelo filtro; sem token ou com token
inválido/expirado recebem 401.

**Independent Test**: `GET /api/v1/qualquer-rota-protegida` sem token → 401; com
token válido → não é 401; com token adulterado → 401.

### Testes — US3 ⚠️ ESCREVER ANTES DA IMPLEMENTAÇÃO

- [ ] T025 [P] [US3] Adicionar cenários de proteção de rotas ao `AuthControllerTest.java`: requisição sem token retorna 401, com token válido não retorna 401, com token adulterado retorna 401 em `fitflow-backend/src/test/java/com/fitflow/controller/AuthControllerTest.java`

### Validação — US3

- [ ] T026 [US3] Verificar que `SecurityConfig.java` bloqueia todas as rotas não listadas em `permitAll()` — confirmar via inspeção do código e resultado dos testes T025 em `fitflow-backend/src/main/java/com/fitflow/config/SecurityConfig.java`

**Checkpoint**: Todas as 3 user stories funcionais. Rodar `./mvnw test` — 100% de testes passando.

---

## Phase 6: Polish & Validação Final

**Purpose**: Garantia de qualidade e validação manual completa.

- [ ] T027 Executar suite completa de testes com `./mvnw test` e confirmar que todos passam: `JwtServiceTest` (5 casos), `UserServiceTest` (2 casos), `AuthServiceTest` (7 casos), `AuthControllerTest` (8 casos) — total mínimo 22 casos de teste
- [ ] T028 Executar checklist de validação manual completa do `specs/001-jwt-auth/quickstart.md` (todos os 12 itens do checklist)

---

## Dependências e Ordem de Execução

### Dependências entre fases

- **Setup (Phase 1)**: sem dependências — começar aqui
- **Foundational (Phase 2)**: depende de Phase 1 — **BLOQUEIA** todas as user stories
- **US1 (Phase 3)**: depende de Phase 2 — MVP mínimo
- **US2 (Phase 4)**: depende de Phase 2 + US1 (AuthService já existe)
- **US3 (Phase 5)**: depende de Phase 2 (SecurityConfig e JwtAuthenticationFilter)
- **Polish (Phase 6)**: depende de todas as fases anteriores

### Dentro de cada fase

- Tarefas de TESTE MUST ser escritas e devem **falhar** antes da implementação
- Para a Foundational: respeitar a sequência T003 → T004 → ... → T016
- Tarefas marcadas [P] dentro da mesma fase podem rodar em paralelo

### Oportunidades de paralelismo na Foundational

```
Paralelo (sem dependência entre si):
  T003 (User.java)
  T005 (RegisterRequestDTO)
  T006 (LoginRequestDTO)
  T007 (AuthResponseDTO)
  T008 (EmailAlreadyExistsException)
  T010 (JwtServiceTest)
  T012 (UserServiceTest)

Sequencial (dependem de tarefas anteriores):
  T004 → depende de T003
  T009 → depende de T008
  T011 → depende de T010 (escrever teste, depois implementar)
  T013 → depende de T012
  T014 → depende de T003, T013
  T015 → depende de T014, T011
  T016 → depende de T011, T014
```

---

## Estratégia de Implementação

### MVP Mínimo (apenas US1)

1. Completar Phase 1 (Setup)
2. Completar Phase 2 (Fundação) — CRÍTICO
3. Completar Phase 3 (US1 - Cadastro)
4. **PARAR E VALIDAR**: testar cadastro end-to-end
5. O sistema já é utilizável para uma funcionalidade real

### Entrega Incremental

1. Phase 1 + 2 → Fundação pronta
2. Phase 3 → Cadastro funcional (MVP!)
3. Phase 4 → Login funcional
4. Phase 5 → Proteção de rotas completa
5. Phase 6 → Validação total

---

## Resumo

| Fase | Tasks | Paralelizáveis | Testes |
|---|---|---|---|
| Setup | T001–T002 | 0 | 0 |
| Foundational | T003–T016 | 7 | 2 (T010, T012) |
| US1 - Cadastro | T017–T020 | 2 | 2 (T017, T018) |
| US2 - Login | T021–T024 | 2 | 2 (T021, T022) |
| US3 - Proteção | T025–T026 | 1 | 1 (T025) |
| Polish | T027–T028 | 0 | 0 |
| **TOTAL** | **28 tasks** | **12** | **7 tasks de teste** |

**Casos de teste planejados**: mínimo 22 (5 JwtService + 2 UserService + 7 AuthService + 8 AuthController)
