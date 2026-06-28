# Research: Autenticação JWT — FitFlow

**Feature**: 001-jwt-auth
**Date**: 2026-06-28

---

## 1. JJWT 0.12.x — Mudanças de API

**Decision**: Usar JJWT 0.12.5 (já no pom.xml).

**Rationale**: A versão 0.12.x quebrou a API da 0.11.x em pontos importantes que afetam
como geramos e validamos tokens.

**Diferenças críticas da 0.11.x → 0.12.x**:

| Operação | 0.11.x (antigo) | 0.12.x (atual) |
|---|---|---|
| Criar chave HMAC | `Keys.hmacShaKeyFor(secret.getBytes())` | `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))` |
| Assinar token | `.signWith(key, SignatureAlgorithm.HS256)` | `.signWith(key)` (algoritmo inferido da chave) |
| Parsear token | `.parseClaimsJws(token)` | `.parseSignedClaims(token)` |
| Acessar body | `.getBody()` | `.getPayload()` |

**Implicação**: O `JWT_SECRET` em `application.yml` deve ser uma string codificada em
Base64, gerada com comprimento suficiente para HS256 (mínimo 256 bits = 32 bytes → 44
chars em Base64).

**Geração do secret (apenas para desenvolvimento)**:
```bash
openssl rand -base64 64
```

---

## 2. Spring Security — Configuração Stateless

**Decision**: Desabilitar sessões HTTP, CSRF e form-login. Usar apenas filtro JWT.

**Rationale**: APIs REST stateless não usam cookies de sessão. O JWT é o único
mecanismo de identidade.

**Configuração necessária**:
- `SessionCreationPolicy.STATELESS` — Spring Security não cria nem usa HttpSession
- `csrf().disable()` — CSRF só é relevante para autenticação baseada em cookies/sessões
- `formLogin().disable()` e `httpBasic().disable()` — desabilita as páginas de login
  padrão do Spring Security que apareceriam ao acessar rotas protegidas
- `authenticationProvider(daoAuthenticationProvider)` — define como o Spring Security
  autentica: busca o usuário pelo e-mail, verifica a senha com BCrypt

**Ordem dos filtros**: `JwtAuthenticationFilter` deve ser adicionado
`BEFORE UsernamePasswordAuthenticationFilter` na cadeia de filtros.

---

## 3. BCrypt — Configuração e Considerações

**Decision**: Usar `BCryptPasswordEncoder` com fator de custo padrão (10).

**Rationale**: Spring Security inclui BCrypt nativamente. O fator 10 representa um
equilíbrio adequado entre segurança e performance (~100ms por hash em hardware moderno).

**Bean necessário** em `ApplicationConfig`:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Alternativas consideradas**: Argon2 (mais seguro, mas exige dependência extra e
configuração adicional — fora do escopo do MVP).

---

## 4. Estrutura dos Claims JWT

**Decision**: Token contém apenas o mínimo necessário.

| Claim | Valor | Motivo |
|---|---|---|
| `sub` | email do usuário | identificador único e imutável |
| `iat` | timestamp de emissão | auditoria |
| `exp` | iat + 24h | expiração conforme spec |

**Claims customizados não serão adicionados agora** (ex: role, userId). O `UserRepository`
é chamado a cada request autenticado para carregar o usuário completo com seus dados
atualizados — isso é o comportamento padrão e correto para stateless JWT.

---

## 5. ProblemDetail (RFC 7807) — Spring 6.x

**Decision**: Usar `ProblemDetail` nativo do Spring 6 (já disponível no Spring Boot 3.x).

**Rationale**: Spring Boot 3.x inclui suporte nativo a `ProblemDetail` sem dependências
extras. O `@RestControllerAdvice` com `@ExceptionHandler` retorna `ProblemDetail`.

**Habilitar no application.yml**:
```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

**Alternativas consideradas**: Criar wrapper customizado de erro — descartado pois
`ProblemDetail` atende RFC 7807 nativamente e evita boilerplate.

---

## 6. Proteção contra Enumeração de Usuários

**Decision**: Retornar sempre `"Credenciais inválidas"` para e-mail não encontrado
E para senha incorreta — sem distinguir os dois casos.

**Rationale**: Mensagens distintas ("e-mail não encontrado" vs "senha incorreta")
permitem que atacantes descubram quais e-mails estão cadastrados no sistema
(user enumeration attack).

**Implementação**: No `AuthService.login()`, o `AuthenticationManager` do Spring
Security já lança `BadCredentialsException` para ambos os casos quando configurado
com `DaoAuthenticationProvider`. O `GlobalExceptionHandler` mapeia esta exceção
para a mensagem genérica.

---

## 7. Trim + Lowercase do E-mail

**Decision**: Normalizar o e-mail (`trim()` + `toLowerCase()`) no `AuthService`
antes de qualquer validação ou persistência.

**Rationale**: `"User@Email.COM "` e `"user@email.com"` devem ser tratados como o
mesmo e-mail. Fazer isso na camada de serviço (não no DTO) garante consistência
independente do cliente.
