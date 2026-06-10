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

## Development Login

After Prompt 03, the local seed admin is:

- Username: `admin`
- Password: `admin123`
