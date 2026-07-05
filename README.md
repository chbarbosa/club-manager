# Club Manager

Single-club sports management application. This repository currently contains
the Prompt 01 scaffold: a Spring Boot backend and a React/Vite frontend.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js and npm
- Docker Desktop or Docker Engine with Compose

## Run the backend

```powershell
cd D:\workspace\club-manager\backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. Health is available at
`http://localhost:8080/actuator/health`. Local H2 data is stored under
`data/` at the repository root and is ignored by Git.

## Backend Observability

Local Actuator URLs:

- Health: `http://localhost:8080/actuator/health`
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Metrics catalog: `http://localhost:8080/actuator/metrics`
- Prometheus scrape output: `http://localhost:8080/actuator/prometheus`

Only health endpoints are public. Metrics, Prometheus, loggers, and info follow
the admin-protected actuator surface. Zipkin export remains disabled by default.

Business and security metric names:

- `club.auth.login.success`
- `club.auth.login.failure`
- `club.auth.login.blocked`
- `club.validation.failure`
- `club.access.denied`
- `club.schedule.created`
- `club.schedule.canceled`
- `club.evaluation.started`
- `club.evaluation.finalized`
- `club.evaluation.event.completed`
- `club.audit.event.recorded`
- `club.match.analysis.saved`

## Backend Security

Local development keeps the seeded admin login available:

- Username: `admin`
- Password: `admin123`

Local startup uses the `dev` Spring profile by default. In that profile, the
backend creates or refreshes a trainer login:

- Username: `trainer@clubmanager.com`
- Password: `pass123`

Example local startup:

```powershell
mvn spring-boot:run
```

For production, explicitly run with `SPRING_PROFILES_ACTIVE=prod` or
`--spring.profiles.active=prod`.

New admin passwords must be 10-128 characters and include uppercase,
lowercase, and a digit, with no whitespace. Login rate limiting is enabled by
default with 5 failed attempts per username and client IP over 15 minutes.

Security configuration:

- `JWT_SECRET`: required in production; must be Base64 and decode to at least
  256 bits. The development fallback is rejected when the `prod` profile is
  active.
- `JWT_EXPIRATION_MS`: defaults to `86400000`.
- `CORS_ALLOWED_ORIGINS`: defaults to `http://localhost:5173`.
- `ADMIN_PASSWORD_MIN_LENGTH`: defaults to `10`.
- `LOGIN_RATE_LIMIT_ENABLED`: defaults to `true`.
- `LOGIN_RATE_LIMIT_MAX_FAILURES`: defaults to `5`.
- `LOGIN_RATE_LIMIT_WINDOW_MINUTES`: defaults to `15`.
- `LOGIN_RATE_LIMIT_STORAGE`: defaults to `in-memory`; use `redis` for
  elastic/cloud deployments where multiple backend instances must share
  throttling counters.
- `SUPPORT_ACCESS_ENABLED`: defaults to `false`. Support access is implemented
  but disabled for this version; set it to `true` only when the feature should
  be exposed again.

## Backend Email Delivery

Trainer access and trainer password reset notifications use the backend
`AccessEmailService`. Support access email delivery is also implemented, but
support access is disabled for this version unless `SUPPORT_ACCESS_ENABLED=true`.

By default, `MAIL_ENABLED=false`, so the app logs that a notification was
prepared without logging codes or temporary passwords. To send real email,
configure SMTP:

- `MAIL_ENABLED=true`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
- `MAIL_SMTP_AUTH=true`
- `MAIL_SMTP_STARTTLS_ENABLE=true`
- `APP_URL`, for links in emails, defaults to `http://localhost:5173`

Never send passwords, JWTs, or confirmation codes to application logs.

## Reports And Exports

Admin CSV exports are available from the UI and API:

- `GET /api/v1/reports/players.csv`
- `GET /api/v1/reports/teams/{teamUuid}/roster.csv`
- `GET /api/v1/reports/schedules.csv`
- `GET /api/v1/reports/championships.csv`
- `GET /api/v1/reports/evaluations/{evaluationUuid}/results.csv`
- `GET /api/v1/reports/teams/{teamUuid}/matches/{matchUuid}/analysis.csv`

## Run the frontend

```powershell
cd D:\workspace\club-manager\frontend
npm.cmd install
npm.cmd run dev
```

The SPA runs at `http://localhost:5173`.

## Run With Docker Compose

The local Docker setup starts the React SPA, Spring Boot backend, Redis, and
persistent H2 file data:

```powershell
cd D:\workspace\club-manager
docker compose up --build
```

Local Docker URLs:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Login: `admin` / `admin123`

The backend container stores H2 files in `/app/data`, mounted from the
repository `data/` directory. Redis is used for login rate limiting in Compose
with `LOGIN_RATE_LIMIT_STORAGE=redis`.

PostgreSQL deployment is intentionally fail-closed. A `postgres` Spring profile
and PostgreSQL/Flyway driver dependencies exist, but startup blocks non-H2
datasources unless `DATABASE_ALLOW_NON_H2=true` is explicitly set. Current
Flyway migrations contain H2-specific SQL such as `RANDOM_UUID()`,
`AUTO_INCREMENT`, and `IF NOT EXISTS` constraint clauses, so PostgreSQL still
needs a dedicated vendor-specific migration slice before real use. The current
audit and migration strategy are documented in
`docs/POSTGRESQL_MIGRATION_AUDIT.md`.

PostgreSQL profile variables reserved for that later slice:

- `SPRING_PROFILES_ACTIVE=postgres`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `DATABASE_ALLOW_NON_H2=false` by default

## Test

```powershell
cd D:\workspace\club-manager\backend
mvn test

cd D:\workspace\club-manager\frontend
npm.cmd run build
npm.cmd run test:e2e:list
```

## CI

GitHub Actions runs a quality gate on pushes to `main`/`master` and on pull
requests:

- backend Maven tests with Java 21;
- frontend install, production build, and Playwright test discovery with Node 20;
- Docker Compose configuration validation.
- backend and frontend Docker image builds without publishing.

The current automation is CI plus deployment readiness. It does not publish
images or deploy to a cloud provider. A future CD slice can publish versioned
images to a private registry and deploy them after registry, environment, and
secret-management decisions are made.

Authentication is available for club admins. Player, trainer, team
registration, current team roster assignment, evaluation groups, evaluation
events, attendance, event skill-level recording, player skill history, final
evaluation results, schedules, championships, match analysis, audit logging,
observability metrics, security hardening, and CSV exports are available.

Admin accounts are deactivated instead of hard-deleted so future history and audit
relationships can remain intact.

Future product and hardening ideas are tracked in `docs/ROADMAP.md`.
