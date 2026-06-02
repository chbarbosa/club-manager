# CLAUDE.md — Club Manager Project

## Project Overview
A single-tenant SaaS solution for small city sports clubs to manage players, trainers, teams, and evaluations. Each club deployment is fully isolated (own infrastructure, own database). The reference implementation is soccer, but the architecture is sport-agnostic.

---

## Architecture

### Backend — Spring Boot Mini-Monolith
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: H2 in-memory
- **ORM**: Spring Data JPA (Hibernate)
- **Migrations**: Flyway
- **Auth**: Spring Security + JWT (stateless)
- **REST**: Versioned API `/api/v1/...`
- **Observability**: Spring Boot Actuator + Micrometer
- **Tracing**: Micrometer Tracing + Brave (traceId/spanId in all logs)
- **Logging**: SLF4J + Logback (structured, includes traceId)
- **Build**: Maven

### Frontend — React SPA
- **Framework**: React 18
- **Styling**: Bootstrap 5 + `global.css` (CSS variables from club config)
- **HTTP**: Axios
- **Auth**: JWT stored in memory (not localStorage)
- **Testing**: Playwright (Node.js) for E2E

---

## Security Rules
- **Never expose internal database `id` (Long) over the API or frontend**
- All entities use `uuid` (UUID v4) as the public identifier
- All API endpoints and frontend routes use `uuid` exclusively
- Internal `id` is used only for JPA relationships and DB performance

---

## Database Design Principles
- Every entity has: `id` (Long, PK, internal only), `uuid` (UUID, public, unique, not null)
- Soft concepts (evaluation levels, federative units) are stored in `club_setup` table as `type` + `json_data` (no extra tables)
- All history is preserved — no hard deletes on historical data (championships, rosters, skill history)

### `club_setup` types
| Type | Example JSON |
|---|---|
| `EVALUATION_LEVEL` | `["Debutant", "Advanced", "Skilled"]` |
| `FEDERATIVE_UNIT` | `["Quebec", "Ontario", "Alberta"]` |

---

## REST API Conventions
- Base path: `/api/v1/`
- Resource naming: plural nouns (`/players`, `/trainers`, `/teams`)
- UUIDs in path: `/api/v1/players/{uuid}`
- HTTP verbs: GET, POST, PUT, PATCH, DELETE
- Responses: always return UUID-based DTOs, never JPA entities directly
- Error responses: structured JSON `{ "error": "...", "message": "...", "traceId": "..." }`
- Pagination: Spring Data `Pageable` for list endpoints

---

## Testing Strategy
- **Unit tests**: JUnit 5 — Service layer only, mocked repositories
- **Integration tests**: MockMvc — All controllers, full request/response cycle
- **E2E tests**: Playwright (Node.js) — All main user flows
- Test naming convention: `MethodName_StateUnderTest_ExpectedBehavior`

---

## Observability
- `/actuator/health` — liveness
- `/actuator/metrics` — Micrometer metrics
- `/actuator/loggers` — runtime log level changes
- Every log line includes `traceId` and `spanId`
- Every REST request is traced end-to-end (controller → service → repository)

---

## Frontend Theming
- On app startup, frontend fetches club config from `/api/v1/club`
- Club `colour1` and `colour2` are applied as CSS variables: `--club-primary` and `--club-secondary`
- Default theme: dark grey (`#2d2d2d`) and light grey (`#f0f0f0`) if no config exists
- All Bootstrap overrides go through `global.css` using these CSS variables

---

## Entity Summary

| Entity | Key Fields |
|---|---|
| `club` | uuid, name, description, colour1, colour2 |
| `club_setup` | uuid, type, json_data |
| `admin` | uuid, name, email, username, passwordHash |
| `player` | uuid, name, birthCountry, livingCountry, birthdate, gender, registrationNumber (nullable), registerDate, memberSince |
| `trainer` | uuid, name, registerDate, memberSince |
| `team` | uuid, ageGroup, gender, trainer |
| `player_team` | uuid, player, team, assignedDate |
| `evaluation` | uuid, title, status (OPEN/IN_PROGRESS/FINALIZED), team |
| `evaluation_player` | uuid, evaluation, player |
| `evaluation_event` | uuid, evaluation, date, status (SCHEDULED/COMPLETED/CANCELED), cancelReason |
| `evaluation_event_attendance` | uuid, evaluationEvent, player, status (PRESENT/ABSENT), reason |
| `evaluation_result` | uuid, evaluation, player, levelResult |
| `field` | uuid, name, location |
| `schedule` | uuid, team, field, dateTime |
| `player_skill_history` | uuid, player, level, changedAt, changedBy (admin), description |
| `championship` | uuid, name, description, team, startMonth, startYear, endMonth, endYear |
| `championship_roster` | uuid, championship, player, trainer |
| `export_job` | uuid, status, createdAt, payload (JSON) |

---

## Project Structure

```
club-manager/
├── backend/
│   └── src/
│       ├── main/java/com/clubmanager/
│       │   ├── config/          # Security, JWT, CORS, Actuator
│       │   ├── domain/          # JPA Entities
│       │   ├── repository/      # Spring Data repositories
│       │   ├── service/         # Business logic
│       │   ├── controller/      # REST controllers (v1)
│       │   ├── dto/             # Request/Response DTOs
│       │   ├── mapper/          # Entity <-> DTO mappers
│       │   └── exception/       # Global exception handler
│       ├── main/resources/
│       │   ├── db/migration/    # Flyway scripts
│       │   └── application.yml
│       └── test/
│           ├── service/         # Unit tests
│           └── controller/      # MockMvc E2E tests
└── frontend/
    ├── src/
    │   ├── api/                 # Axios instances
    │   ├── components/          # Reusable components
    │   ├── pages/               # Route pages
    │   ├── context/             # Auth + Club theme context
    │   └── global.css           # CSS variables + Bootstrap overrides
    └── tests/
        └── e2e/                 # Playwright tests
```

---

## Code Style
- DTOs must never contain `id` (Long) — only `uuid` and business fields
- All controller methods must be covered by a MockMvc test
- All service methods must be covered by a unit test
- Use `@Transactional` on service methods that write to DB
- Flyway scripts named: `V{version}__{description}.sql`
