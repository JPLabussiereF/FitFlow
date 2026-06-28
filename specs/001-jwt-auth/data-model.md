# Data Model: Autenticação JWT — FitFlow

**Feature**: 001-jwt-auth
**Date**: 2026-06-28

---

## Entidades

### User (tabela: `users`)

**Status da migration**: ✅ Já existe — `V1__create_users_table.sql` — nenhuma nova
migration necessária para esta feature.

**Schema existente (confirmado)**:
```sql
CREATE TABLE users (
    id         BIGSERIAL                NOT NULL,
    name       VARCHAR(100)             NOT NULL,
    email      VARCHAR(255)             NOT NULL,
    password   VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users        PRIMARY KEY (id),
    CONSTRAINT uq_users_email  UNIQUE      (email)
);
```

**Entidade JPA** (`com.fitflow.model.User`):

```
User
├── id          : Long          @Id @GeneratedValue(IDENTITY)
├── name        : String        @Column(nullable=false, length=100)
├── email       : String        @Column(nullable=false, unique=true, length=255)
├── password    : String        @Column(nullable=false)  ← hash BCrypt, nunca texto puro
├── createdAt   : Instant       @CreationTimestamp @Column(updatable=false)
└── updatedAt   : Instant       @UpdateTimestamp
```

**Implementa**: `UserDetails` (Spring Security)
- `getUsername()` → retorna `email`
- `getPassword()` → retorna `password` (hash)
- `getAuthorities()` → lista vazia (sem roles no MVP)
- `isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`, `isEnabled()` → todos `true`

**Anotações Lombok**:
- `@Data` — getters, setters, equals, hashCode, toString
- `@Builder` — padrão builder para criação
- `@NoArgsConstructor` — necessário para JPA
- `@AllArgsConstructor` — necessário para @Builder

---

## DTOs

### RegisterRequestDTO
```
RegisterRequestDTO
├── name     : String   @NotBlank @Size(min=1, max=100)
├── email    : String   @NotBlank @Email @Size(max=255)
└── password : String   @NotBlank @Size(min=6, max=72)
```

### LoginRequestDTO
```
LoginRequestDTO
├── email    : String   @NotBlank @Email
└── password : String   @NotBlank
```

### AuthResponseDTO
```
AuthResponseDTO
├── token  : String   ← JWT gerado
├── userId : Long
├── name   : String
└── email  : String
```

---

## Token JWT

**Não é persistido** — existe apenas em memória durante a vida da requisição (geração)
e no cliente (armazenamento pós-login).

**Estrutura dos claims**:
```json
{
  "sub": "user@email.com",
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Parâmetros**:
- Algoritmo: HS256 (HMAC-SHA256)
- Expiração: 86.400 segundos (24 horas)
- Secret: variável de ambiente `JWT_SECRET` (Base64, mínimo 44 chars)

---

## Relacionamentos desta feature

Nenhum relacionamento entre tabelas para esta feature. A tabela `users` é criada de
forma isolada. Os relacionamentos com `workouts`, `exercises`, etc. serão definidos
nas migrations e entidades das features seguintes.

---

## Pacotes Java

```
com.fitflow/
├── config/
│   ├── ApplicationConfig.java     ← @Bean: PasswordEncoder, AuthenticationManager
│   └── SecurityConfig.java        ← @Bean: SecurityFilterChain (rotas + filtros)
├── controller/
│   └── AuthController.java        ← POST /api/v1/auth/register + /login
├── dto/
│   └── auth/
│       ├── RegisterRequestDTO.java
│       ├── LoginRequestDTO.java
│       └── AuthResponseDTO.java
├── exception/
│   ├── EmailAlreadyExistsException.java  ← RuntimeException customizada
│   └── GlobalExceptionHandler.java       ← @RestControllerAdvice + ProblemDetail
├── model/
│   └── User.java                  ← @Entity + implements UserDetails
├── repository/
│   └── UserRepository.java        ← extends JpaRepository<User, Long>
│                                     + findByEmail(String): Optional<User>
├── security/
│   └── JwtAuthenticationFilter.java  ← extends OncePerRequestFilter
└── service/
    ├── AuthService.java           ← register(), login() — lógica de negócio
    ├── JwtService.java            ← generateToken(), validateToken(), extractUsername()
    └── UserService.java           ← implements UserDetailsService: loadUserByUsername()
```

---

## Fluxo de Dados — Cadastro

```
Cliente → POST /api/v1/auth/register (RegisterRequestDTO)
  → AuthController.register()
    → AuthService.register(dto)
      → normaliza email (trim + toLowerCase)
      → valida unicidade via UserRepository.findByEmail()
      → gera hash BCrypt da senha
      → salva User via UserRepository.save()
      → gera JWT via JwtService.generateToken(user)
      → retorna AuthResponseDTO
  → 201 Created + AuthResponseDTO
```

## Fluxo de Dados — Login

```
Cliente → POST /api/v1/auth/login (LoginRequestDTO)
  → AuthController.login()
    → AuthService.login(dto)
      → normaliza email (trim + toLowerCase)
      → chama AuthenticationManager.authenticate()
          (busca user pelo email + verifica senha com BCrypt)
      → se falhar: lança BadCredentialsException → 401 "Credenciais inválidas"
      → carrega User via UserRepository.findByEmail()
      → gera JWT via JwtService.generateToken(user)
      → retorna AuthResponseDTO
  → 200 OK + AuthResponseDTO
```

## Fluxo de Dados — Requisição Protegida

```
Cliente → GET /api/v1/qualquer-rota (Authorization: Bearer <token>)
  → JwtAuthenticationFilter.doFilterInternal()
    → extrai token do header
    → extrai email via JwtService.extractUsername(token)
    → valida token via JwtService.validateToken(token, userDetails)
    → se válido: seta UsernamePasswordAuthenticationToken no SecurityContext
  → Controller recebe a requisição autenticada
```
