# Club Manager

Single-club sports management application. This repository currently contains
the Prompt 01 scaffold: a Spring Boot backend and a React/Vite frontend.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js and npm

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

## Run the frontend

```powershell
cd D:\workspace\club-manager\frontend
npm.cmd install
npm.cmd run dev
```

The SPA runs at `http://localhost:5173`.

## Test

```powershell
cd D:\workspace\club-manager\backend
mvn test

cd D:\workspace\club-manager\frontend
npm.cmd run build
npm.cmd run test:e2e:list
```

Authentication is available for club admins. Player, trainer, team
registration, current team roster assignment, evaluation groups, evaluation
events, attendance, event skill-level recording, player skill history, and
final evaluation results are available.

Admin accounts are deactivated instead of hard-deleted so future history and audit
relationships can remain intact.

Future product and hardening ideas are tracked in `docs/ROADMAP.md`.
