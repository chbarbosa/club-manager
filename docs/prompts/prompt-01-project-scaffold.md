# Prompt 01 — Project Scaffold

## Goal
Generate the full project structure for the Club Manager application: Spring Boot backend + React frontend, with all dependencies, configuration, and base setup ready to build features on top of.

---

## Instructions

Read `CLAUDE.md` before starting. Follow all conventions defined there.

### Backend — Spring Boot

Generate a Maven project with the following:

**Dependencies (`pom.xml`):**
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator`
- `spring-boot-starter-validation`
- `com.h2database:h2`
- `org.flywaydb:flyway-core`
- `io.micrometer:micrometer-tracing-bridge-brave`
- `io.zipkin.reporter2:zipkin-reporter-brave`
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (version 0.11.5)
- `org.mapstruct:mapstruct` + `mapstruct-processor`
- `org.projectlombok:lombok`
- `spring-boot-starter-test`

**`application.yml`:**
- H2 in-memory datasource
- H2 console enabled at `/h2-console`
- Flyway enabled
- Actuator endpoints exposed: `health`, `metrics`, `loggers`, `info`
- JWT secret and expiration configurable via properties
- Logging pattern includes `traceId` and `spanId`:
  ```
  %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [traceId=%X{traceId}] [spanId=%X{spanId}] %logger{36} - %msg%n
  ```

**Base packages to create (empty, with a placeholder file each):**
- `config/` — SecurityConfig, JwtConfig, CorsConfig
- `domain/` — JPA entities
- `repository/` — Spring Data interfaces
- `service/` — Business logic
- `controller/` — REST controllers
- `dto/` — Request and Response records
- `mapper/` — MapStruct interfaces
- `exception/` — GlobalExceptionHandler

**`GlobalExceptionHandler`:**
- Handle `EntityNotFoundException` → 404
- Handle `ConstraintViolationException` → 400
- Handle generic `Exception` → 500
- All error responses return:
  ```json
  {
    "error": "ERROR_CODE",
    "message": "Human readable message",
    "traceId": "extracted from MDC"
  }
  ```

**Base entity `AbstractEntity`:**
```java
@MappedSuperclass
public abstract class AbstractEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();
}
```
All entities extend this class.

**Flyway:** Create `src/main/resources/db/migration/` folder with a `V1__init.sql` placeholder comment.

---

### Frontend — React

Generate a React 18 project (Vite) with:

**Dependencies (`package.json`):**
- `react`, `react-dom`
- `react-router-dom` v6
- `axios`
- `bootstrap` v5
- Dev: `@playwright/test`, `vite`

**`src/global.css`:**
```css
:root {
  --club-primary: #2d2d2d;
  --club-secondary: #f0f0f0;
}

/* Bootstrap overrides using club variables */
.btn-primary {
  background-color: var(--club-primary);
  border-color: var(--club-primary);
}

.navbar {
  background-color: var(--club-primary) !important;
}
```

**`src/context/ClubContext.jsx`:**
- On mount, fetch `GET /api/v1/club`
- Set `--club-primary` and `--club-secondary` CSS variables on `document.documentElement`
- Fallback to default grey values if fetch fails

**`src/context/AuthContext.jsx`:**
- Store JWT in memory (React state only — never localStorage)
- Expose `login(token)`, `logout()`, `isAuthenticated()`

**`src/api/axios.js`:**
- Axios instance with `baseURL: /api/v1`
- Request interceptor: attach `Authorization: Bearer {token}` from AuthContext
- Response interceptor: on 401, call `logout()`

**`src/App.jsx`:**
- Wrap app in `ClubContext` and `AuthContext`
- Router with placeholder routes: `/login`, `/dashboard`

**Playwright config (`playwright.config.js`):**
- Base URL: `http://localhost:5173`
- One browser: chromium
- Test directory: `tests/e2e/`

---

## Expected Output
- Full runnable project scaffold
- `mvn spring-boot:run` starts the backend on port 8080
- `npm run dev` starts the frontend on port 5173
- No feature logic yet — just the skeleton ready for feature prompts
