<!--
  SYNC IMPACT REPORT
  Version change: 0.0.0 (template) → 1.0.0 (initial ratification)
  Added sections: Core Principles (I–VI), Stack & Constraints, Development Workflow,
                  Data Model Decisions, Backlog (Post-MVP), Governance
  Removed sections: N/A (initial version)
  Templates updated:
    ✅ .specify/templates/plan-template.md — Constitution Check gates updated implicitly
    ✅ .specify/templates/spec-template.md — no changes required; template is generic
    ✅ .specify/templates/tasks-template.md — no changes required; template is generic
  Follow-up TODOs: none
-->

# FitFlow Constitution

## Core Principles

### I. Test-First (NON-NEGOTIABLE)

Every component with business logic MUST have tests written before the implementation is
considered done. There are no exceptions.

- **Backend**: JUnit 5 + Mockito for unit tests; MockMvc for controller tests.
- **Frontend**: Jasmine + TestBed for Angular components and services.
- Minimum coverage: **80%** for backend service and controller layers.
- Every check (feature delivery) MUST include: happy path, invalid input, and error scenario.
- Tests are never left as "to do later" — a feature without tests is not done.

### II. API-First

Every backend feature is defined by its HTTP contract before implementation.

- All endpoints MUST follow REST conventions under the `/api/v1/` prefix.
- Error responses MUST use **ProblemDetail (RFC 7807)**: `type`, `title`, `status`, `detail`.
- Pagination follows a standard envelope: `content`, `page`, `size`, `totalElements`, `totalPages`, `last`.
- Pagination is REQUIRED for: `GET /api/exercises`, `GET /api/workouts`, `GET /api/sessions/history`.
- Default page size: 20 items.

### III. Layered Architecture (Backend)

The backend MUST follow a strict three-layer architecture. No layer may skip another.

```
Controller → Service → Repository
```

- **Controllers**: HTTP only — parse request, delegate to service, return response. Zero business logic.
- **Services**: All business rules live here. No HTTP types (`HttpServletRequest`, etc.).
- **Repositories**: Data access only via Spring Data JPA. No business logic.
- **DTOs** (request/response) and **Entities** (JPA) are separate classes, always. MapStruct converts between them.
- Validation annotations (`@Valid`, `@NotBlank`, etc.) belong on DTOs, never on JPA entities.

### IV. Security-First

Security rules are non-negotiable and apply from the first commit.

- Authentication: **JWT stateless** via Spring Security. No sessions, no cookies.
- Passwords: always hashed with **BCrypt**. Never stored in plain text, never logged.
- Secrets (JWT secret, DB password): always via environment variables. Never hardcoded.
- Public routes: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`.
- All other routes require a valid Bearer token.
- CSRF disabled (stateless API).
- Sensitive data (passwords, tokens) MUST NOT appear in logs at any level.

### V. Schema Ownership via Flyway

The database schema is owned exclusively by Flyway. No exceptions.

- `spring.jpa.hibernate.ddl-auto` MUST be set to `validate` (or `none`) — never `create`, `update`, or `create-drop`.
- Every schema change requires a new numbered migration script (`V{n}__description.sql`).
- Migration scripts are immutable once committed. Never edit an existing migration.
- No stored procedures, triggers, or business logic in the database. SQL is for structure and data only.

### VI. Frontend Architecture (Angular)

The Angular frontend MUST follow modern Angular patterns.

- All components MUST be **standalone** (no NgModules for feature code).
- State management MUST use **Angular Signals**. Avoid `BehaviorSubject` for local component state.
- Feature modules MUST use **lazy loading** via Angular Router.
- UI library: **Angular Material** with the project's custom purple theme.
- Charts: **Chart.js via ng2-charts**.
- Backend communication: exclusively via Angular `HttpClient` with typed DTOs. No direct DOM manipulation.
- `environment.ts` defines the API base URL; never hardcode URLs in services.

---

## Stack & Technology Constraints

| Layer | Technology | Version |
|---|---|---|
| Language (backend) | Java | 17 |
| Framework (backend) | Spring Boot | 3.x |
| ORM | Spring Data JPA + Hibernate | — |
| Database | PostgreSQL | 16 |
| Migrations | Flyway | — |
| Security | Spring Security + JJWT | — |
| Boilerplate reduction | Lombok | — |
| DTO mapping | MapStruct | — |
| Build tool | Maven | — |
| Language (frontend) | TypeScript | 5.x |
| Framework (frontend) | Angular | 17+ |
| UI components | Angular Material | — |
| Charts | ng2-charts (Chart.js) | — |
| Infra (local) | Docker + Docker Compose | — |
| CI | GitHub Actions | — |

---

## Data Model Decisions

These decisions were made during the spec phase and are binding. Changing them requires a
constitution amendment.

**Exercise ownership**
- `exercises.is_system = true`: global exercises visible to all users; no user may edit or delete them via API.
- `exercises.created_by` (FK → `users.id`): user-created exercises, visible and editable only by their creator.
- `exercises.image_url`: optional, free-form URL. Frontend MUST display a muscle-group icon fallback when this field is null, empty, or returns an HTTP error.
- `exercises.updated_at`: MUST be updated on every edit.

**Workout templates**
- `workouts.is_template = true` + `workouts.user_id = null`: system templates, read-only for all users.
- Users may **clone** a template to their account (`is_template = false`, `user_id = current user`).
- Without cloning, users may still execute sessions from any template, but cannot modify it.

**Session model (Treino Vivo)**
- `workout_sessions` holds a **live** FK to `workouts`. There is no snapshot of the workout plan.
- `session_sets` records actual user performance (exercise_id, set_number, reps_done, weight_kg, optional rest_seconds, optional notes, performed_at).
- Users add `session_sets` dynamically during execution; there is no pre-generated set list.
- Because `session_sets.exercise_id` is a direct FK to `exercises`, historical records are preserved even if the linked workout is later edited.

**Pagination**
- Standard response envelope for all paginated endpoints (see Principle II above).

---

## Development Workflow

- **Branches**: `main` (production), `development` (integration), `feature/<short-name>`.
- **Flow**: `feature/X` → PR → `development` → CI passes → merge → test locally with Docker → `main`.
- **Commits**: Conventional Commits — `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `ci`.
  - Example: `feat(auth): implement JWT login endpoint`.
- **No push** directly to `main` or `development`. PRs are mandatory.
- **CI** must pass (build + tests) before any merge.
- Every feature starts with a spec in `specs/` before any code is written.

---

## Backlog (Post-MVP — Do Not Implement Now)

> These items are intentionally deferred. Document them here so they are not forgotten.

**Session compaction job**
A `@Scheduled` job that aggregates `session_sets` older than 30 days into a `metrics_snapshots`
table, reducing storage of raw granular data. Requires schema design for `metrics_snapshots`
and careful transaction handling. Add when the project scales beyond its study scope.

---

## Governance

- This constitution supersedes `CLAUDE.md` for all technical architecture decisions. `CLAUDE.md`
  governs AI collaboration behavior.
- Amendments require: (1) a proposal written in this file with rationale, (2) version bump,
  (3) propagation check across all templates in `.specify/templates/`.
- Amendment versioning: MAJOR for principle removal/redefinition; MINOR for new principle or
  section; PATCH for clarifications.
- Every spec and plan MUST include a "Constitution Check" gate before implementation begins.
- If a task appears to violate a principle, raise it explicitly rather than working around it silently.

**Version**: 1.0.0 | **Ratified**: 2026-06-28 | **Last Amended**: 2026-06-28
