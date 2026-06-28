# Quickstart: Testando Autenticação JWT — FitFlow

**Feature**: 001-jwt-auth
**Date**: 2026-06-28

---

## Pré-requisitos

- Docker e Docker Compose instalados
- Java 17+ instalado
- Maven wrapper disponível (`./mvnw`)

---

## 1. Configurar variáveis de ambiente

Crie (ou confirme que existe) um arquivo `.env` na raiz de `fitflow-backend/` com:

```env
# Gere com: openssl rand -base64 64
JWT_SECRET=SEU_SECRET_BASE64_AQUI_MINIMO_44_CHARS

# Banco de dados (deve bater com o docker-compose.yml)
DB_URL=jdbc:postgresql://localhost:5432/fitflow
DB_USERNAME=fitflow
DB_PASSWORD=fitflow
```

> ⚠️ Nunca comite o `.env` com valores reais. O `.gitignore` deve ignorá-lo.

---

## 2. Subir o banco de dados

```bash
# Na raiz do projeto
docker compose up -d

# Verificar que o PostgreSQL está saudável
docker compose ps
```

---

## 3. Rodar o backend

```bash
cd fitflow-backend
./mvnw spring-boot:run
```

Ao iniciar, o Flyway vai rodar automaticamente `V1__create_users_table.sql`.
Você deve ver no log:
```
Flyway Community Edition ... by Redgate
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - create users table"
Successfully applied 1 migration to schema "public"
```

---

## 4. Validar: Cadastro

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Teste",
    "email": "joao@teste.com",
    "password": "senha123"
  }'
```

**Resposta esperada (201)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "João Teste",
  "email": "joao@teste.com"
}
```

---

## 5. Validar: E-mail duplicado

```bash
# Tente cadastrar o mesmo e-mail novamente
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Duplicado",
    "email": "joao@teste.com",
    "password": "senha456"
  }'
```

**Resposta esperada (409)**:
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "E-mail já está em uso",
  "instance": "/api/v1/auth/register"
}
```

---

## 6. Validar: Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@teste.com",
    "password": "senha123"
  }'
```

**Resposta esperada (200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "João Teste",
  "email": "joao@teste.com"
}
```

---

## 7. Validar: Rota protegida sem token

```bash
# Qualquer rota protegida — exemplo fictício pois ainda não há outras rotas
curl http://localhost:8080/api/v1/exercises
```

**Resposta esperada (401)** — sem acesso sem token.

---

## 8. Validar: Rota protegida com token

```bash
# Use o token recebido no login
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl http://localhost:8080/api/v1/exercises \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta esperada**: qualquer coisa exceto 401 (para esta feature, pode ser 404
pois o endpoint de exercícios ainda não existe — o importante é não ser 401).

---

## 9. Rodar os testes

```bash
cd fitflow-backend
./mvnw test
```

**Saída esperada**: todos os testes passando, incluindo:
- `JwtServiceTest`
- `UserServiceTest`
- `AuthServiceTest`
- `AuthControllerTest`

---

## Checklist de Validação Manual

- [ ] `POST /api/v1/auth/register` com dados válidos → 201 + token
- [ ] `POST /api/v1/auth/register` com e-mail duplicado → 409
- [ ] `POST /api/v1/auth/register` com e-mail inválido → 400
- [ ] `POST /api/v1/auth/register` com senha < 6 chars → 400
- [ ] `POST /api/v1/auth/register` com nome vazio → 400
- [ ] `POST /api/v1/auth/login` com credenciais corretas → 200 + token
- [ ] `POST /api/v1/auth/login` com senha errada → 401 "Credenciais inválidas"
- [ ] `POST /api/v1/auth/login` com e-mail inexistente → 401 "Credenciais inválidas"
- [ ] Rota protegida sem token → 401
- [ ] Rota protegida com token válido → não é 401
- [ ] Rota protegida com token expirado/adulterado → 401
- [ ] Todos os testes automatizados passando (`./mvnw test`)
