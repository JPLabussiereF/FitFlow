<div align="center">

# 💪 FitFlow

### Seu personal trainer digital — registre, evolua e visualize seu progresso

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17+-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-7C3AED?style=for-the-badge)](.)

</div>

---

## 📖 Sobre o Projeto

O **FitFlow** é uma aplicação web para gerenciamento de treinos físicos. A ideia é simples: criar, executar e acompanhar seus treinos de forma intuitiva, com histórico completo e métricas de evolução.

> 🎯 **Objetivo principal:** Aprender na prática com um projeto real, usando Spring Boot no back-end e Angular no front-end.

---

## ✨ Funcionalidades (MVP)

- Cadastro e login de usuários com autenticação JWT
- Catálogo de exercícios pré-cadastrados com filtro por grupo muscular
- Criação de treinos A/B/C com "playlist" de exercícios
- Registro de séries, repetições, peso, descanso e anotações por exercício
- Execução de treino com check por exercício e salvamento de sessão
- Histórico de treinos e dashboard de métricas com gráficos

---

## 🏗️ Arquitetura

```
fitflow/
├── fitflow-backend/          # Spring Boot API (REST + JWT)
│   ├── src/main/java/
│   │   └── com/fitflow/
│   │       ├── config/       # Segurança, CORS, JWT
│   │       ├── controller/   # Endpoints REST
│   │       ├── service/      # Regras de negócio
│   │       ├── repository/   # Acesso ao banco (JPA)
│   │       ├── model/        # Entidades JPA
│   │       └── dto/          # Objetos de transferência
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/     # Scripts Flyway
│
└── fitflow-frontend/         # Angular 17 SPA
    └── src/app/
        ├── core/             # Guards, interceptors, serviços globais
        ├── shared/           # Componentes reutilizáveis
        └── features/
            ├── auth/         # Login e Cadastro
            ├── exercises/    # Catálogo de exercícios
            ├── workout/      # Criação e execução de treinos
            └── dashboard/    # Métricas e histórico
```

---

## 🗄️ Modelagem do Banco de Dados

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│    users    │     │   workouts   │     │ workout_sessions │
│─────────────│     │──────────────│     │─────────────────│
│ id (PK)     │────<│ id (PK)      │────<│ id (PK)         │
│ name        │     │ user_id (FK) │     │ workout_id (FK) │
│ email       │     │ name         │     │ started_at      │
│ password    │     │ type (A/B/C) │     │ completed_at    │
│ created_at  │     │ created_at   │     │ notes           │
└─────────────┘     └──────────────┘     └─────────────────┘
                           │                      │
                    ┌──────┴──────┐        ┌──────┴───────────┐
                    │  exercises  │        │   session_sets   │
                    │─────────────│        │──────────────────│
                    │ id (PK)     │        │ id (PK)          │
                    │ name        │        │ session_id (FK)  │
                    │ muscle_group│        │ exercise_id (FK) │
                    │ description │        │ sets             │
                    │ image_url   │        │ reps             │
                    └─────────────┘        │ weight_kg        │
                                           │ rest_seconds     │
                                           │ notes            │
                                           │ completed        │
                                           └──────────────────┘
```

---

## 🚀 Stack Tecnológica

### Back-end
| Tecnologia | Versão | Função |
|---|---|---|
| Java | 17+ | Linguagem principal |
| Spring Boot | 3.x | Framework base da API |
| Spring Security + JWT | — | Autenticação stateless |
| Spring Data JPA | — | Acesso ao banco via objetos |
| PostgreSQL | 16 | Banco de dados relacional |
| Flyway | — | Versionamento de migrations |
| Lombok | — | Reduz boilerplate |
| MapStruct | — | Conversão Entity ↔ DTO |

### Front-end
| Tecnologia | Versão | Função |
|---|---|---|
| Angular | 17+ | Framework SPA |
| Angular Material | — | Componentes UI com tema roxo |
| RxJS | — | Programação reativa |
| Chart.js / ng2-charts | — | Gráficos do dashboard |

### Infraestrutura
| Tecnologia | Uso |
|---|---|
| Docker + Docker Compose | Banco local isolado |
| GitHub Actions | CI automático |
| Railway / Render | Hospedagem do back-end |
| Vercel / Netlify | Hospedagem do front-end |

---

## 🛠️ Como Rodar Localmente

```bash
# 1. Clone o repositório
git clone https://github.com/JPLabussiereF/fitflow.git
cd fitflow

# 2. Suba o banco de dados
docker compose up -d

# 3. Rode o back-end
cd fitflow-backend
./mvnw spring-boot:run

# 4. Rode o front-end
cd fitflow-frontend
npm install && ng serve

# Acesse: http://localhost:4200
```

> Para acompanhar o progresso de desenvolvimento, veja o [ROADMAP.md](ROADMAP.md).

---

## 📝 Licença

Projeto pessoal e educacional. Feito com 💜 para aprender na prática.

---

<div align="center">
  <sub>Construído por <a href="https://github.com/JPLabussiereF">JPLabussiereF</a> • com muito café e vontade de aprender ☕</sub>
</div>