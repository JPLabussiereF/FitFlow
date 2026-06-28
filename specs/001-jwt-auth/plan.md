# Implementation Plan: Autenticação de Usuário com JWT

**Branch**: `001-jwt-auth` | **Date**: 2026-06-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-jwt-auth/spec.md`

---

## Summary

Implementar autenticação stateless com JWT para o FitFlow. O sistema deve permitir
cadastro (com validação e hash BCrypt) e login de usuários, retornando um JWT com
validade de 24 horas. Todas as rotas exceto os dois endpoints de auth devem exigir
token válido no header `Authorization: Bearer <token>`.

Stack: Spring Boot 3.2.5 + Spring Security + JJWT 0.12.5 + PostgreSQL 16 (Flyway).
A tabela `users` já existe (V1). Nenhuma nova migration é necessária para esta feature.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**:
- Spring Boot 3.2.5 (web, security, data-jpa, validation)
- JJWT 0.12.5 (jjwt-api + jjwt-impl + jjwt-jackson)
- PostgreSQL driver + Flyway
- Lombok + MapStruct 1.5.5.Final
- spring-security-test (testes)

**Storage**: PostgreSQL 16 — tabela `users` já criada por `V1__create_users_table.sql`

**Testing**: JUnit 5 + Mockito + MockMvc + spring-security-test

**Target Platform**: Linux server (Docker Compose local)

**Project Type**: REST API backend (web-service)

**Performance Goals**: Sem metas específicas para MVP. BCrypt custo padrão (10)
implica ~100ms por hash — aceitável para fluxo de auth.

**Constraints**:
- JWT stateless — sem sessão no servidor
- Senhas sempre em BCrypt — nunca texto puro, nunca em log
- Secret JWT via variável de ambiente `JWT_SECRET` (Base64, mín. 44 chars)
- Mensagem genérica "Credenciais inválidas" para 401 (anti-enumeração)
- ProblemDetail (RFC 7807) para todos os erros

**Scale/Scope**: Projeto de estudo — sem requisito de escala horizontal no MVP.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate | Status |
|---|---|---|
| I. Test-First | `JwtServiceTest`, `UserServiceTest`, `AuthServiceTest`, `AuthControllerTest` escritos antes/junto da implementação | ✅ Planejado |
| II. API-First | Contrato definido em `contracts/api-spec.json` antes da implementação | ✅ Feito |
| III. Layered Arch | `AuthController → AuthService → UserRepository` — nenhum acesso direto ao repositório fora do service | ✅ Planejado |
| IV. Security-First | BCrypt obrigatório, JWT secret via env var, mensagem genérica no 401, sem dados sensíveis em log | ✅ Planejado |
| V. Flyway | `V1__create_users_table.sql` já existe e é o único dono do schema. `ddl-auto: validate` | ✅ Já atendido |
| VI. Frontend | Fora do escopo desta feature — backend only | ✅ N/A |

**Resultado**: Todos os gates aprovados. Pode prosseguir para implementação.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-jwt-auth/
├── plan.md              ← este arquivo
├── research.md          ← decisões técnicas de JJWT, BCrypt, ProblemDetail
├── data-model.md        ← entidade User, DTOs, fluxos de dados
├── quickstart.md        ← como testar manualmente
├── contracts/
│   └── api-spec.json    ← OpenAPI 3.0 dos 2 endpoints
└── checklists/
    └── requirements.md  ← spec quality checklist (100% pass)
```

### Source Code — Backend

```text
fitflow-backend/src/main/java/com/fitflow/
├── config/
│   ├── ApplicationConfig.java        ← @Bean: PasswordEncoder, AuthenticationManager
│   └── SecurityConfig.java           ← @Bean: SecurityFilterChain
├── controller/
│   └── AuthController.java           ← POST /api/v1/auth/register e /login
├── dto/
│   └── auth/
│       ├── RegisterRequestDTO.java
│       ├── LoginRequestDTO.java
│       └── AuthResponseDTO.java
├── exception/
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java   ← @RestControllerAdvice + ProblemDetail
├── model/
│   └── User.java                     ← @Entity + implements UserDetails
├── repository/
│   └── UserRepository.java           ← findByEmail()
├── security/
│   └── JwtAuthenticationFilter.java  ← OncePerRequestFilter
└── service/
    ├── AuthService.java
    ├── JwtService.java
    └── UserService.java              ← implements UserDetailsService

fitflow-backend/src/test/java/com/fitflow/
├── controller/
│   └── AuthControllerTest.java       ← MockMvc
└── service/
    ├── JwtServiceTest.java
    ├── UserServiceTest.java
    └── AuthServiceTest.java          ← Mockito
```

**Structure Decision**: Web application (backend only para esta feature). Frontend
Angular será coberto em feature separada (002-angular-auth).

---

## Ordem de Implementação

A ordem respeita a arquitetura em camadas (de baixo para cima) e o princípio TDD
(teste antes/junto da implementação de cada componente com lógica de negócio).

### Fase 0 — Infraestrutura de Configuração

Sem lógica de negócio, sem testes unitários nesta camada.

1. `User.java` — entidade JPA + UserDetails (base de tudo)
2. `UserRepository.java` — interface JPA (sem lógica, sem teste unitário)
3. DTOs: `RegisterRequestDTO`, `LoginRequestDTO`, `AuthResponseDTO`
4. `EmailAlreadyExistsException.java` + `GlobalExceptionHandler.java`
5. `ApplicationConfig.java` — beans de PasswordEncoder e AuthenticationManager
6. `SecurityConfig.java` — filter chain, rotas públicas/protegidas
7. `JwtAuthenticationFilter.java` — filtro JWT (depende de JwtService)

### Fase 1 — Serviços (TDD)

Para cada serviço: escrever os testes → confirmar que falham → implementar.

8. **[TEST]** `JwtServiceTest.java` — testa geração, validação, expiração, username
9. **[IMPL]** `JwtService.java` — generateToken(), validateToken(), extractUsername()
10. **[TEST]** `UserServiceTest.java` — testa loadUserByUsername() com Mockito
11. **[IMPL]** `UserService.java` — implements UserDetailsService
12. **[TEST]** `AuthServiceTest.java` — testa register() e login() com Mockito
13. **[IMPL]** `AuthService.java` — register(), login()

### Fase 2 — Controller (TDD)

14. **[TEST]** `AuthControllerTest.java` — 8 cenários com MockMvc
15. **[IMPL]** `AuthController.java` — register() e login()

### Fase 3 — Validação Manual

16. Subir banco + backend e executar checklist do `quickstart.md`

---

## Detalhes dos Testes Planejados

### JwtServiceTest (JUnit 5, sem Spring context)
- `generateToken_shouldReturnNonNullJwt`
- `extractUsername_shouldReturnEmailFromToken`
- `validateToken_shouldReturnTrueForValidToken`
- `validateToken_shouldReturnFalseForExpiredToken`
- `validateToken_shouldReturnFalseForWrongUser`

### UserServiceTest (JUnit 5 + Mockito)
- `loadUserByUsername_shouldReturnUserDetailsWhenEmailExists`
- `loadUserByUsername_shouldThrowWhenEmailNotFound`

### AuthServiceTest (JUnit 5 + Mockito)
- `register_shouldHashPasswordBeforeSaving`
- `register_shouldThrowWhenEmailAlreadyExists`
- `register_shouldReturnAuthResponseWithToken`
- `login_shouldReturnAuthResponseForValidCredentials`
- `login_shouldThrowBadCredentialsForWrongPassword`
- `register_shouldNormalizeEmailBeforeSaving`

### AuthControllerTest (MockMvc)
- `register_shouldReturn201ForValidRequest`
- `register_shouldReturn409ForDuplicateEmail`
- `register_shouldReturn400ForInvalidEmail`
- `register_shouldReturn400ForShortPassword`
- `register_shouldReturn400ForEmptyName`
- `login_shouldReturn200ForValidCredentials`
- `login_shouldReturn401ForWrongPassword`
- `login_shouldReturn401ForUnknownEmail`

---

## Complexidade Tracking

> Sem violações de constituição — seção não aplicável.
