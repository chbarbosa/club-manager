# Prompt 05 — Trainer Registration

## Goal
Implement full trainer registration: create, list, view, update, and deactivate trainers. Trainers are a separate concept from admins. They have registerDate (auto) and memberSince (manual), similar to players.

---

## Instructions

Read `CLAUDE.md` before starting. Follow all conventions defined there.

---

### Backend

**Flyway migration `V6__trainer.sql`:**
```sql
CREATE TABLE trainer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    birth_country VARCHAR(100),
    living_country VARCHAR(100),
    birthdate DATE,
    email VARCHAR(150),
    phone VARCHAR(30),
    register_date DATE NOT NULL,
    member_since DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Entity `Trainer`:**
- Extends `AbstractEntity`
- Fields: `name`, `birthCountry` (nullable), `livingCountry` (nullable), `birthdate` (nullable), `email` (nullable), `phone` (nullable), `registerDate` (LocalDate), `memberSince` (LocalDate), `active` (boolean, default true)

**DTOs:**
- `TrainerCreateRequest`:
  - `name` (not blank)
  - `birthCountry` (nullable)
  - `livingCountry` (nullable)
  - `birthdate` (nullable, must be in the past if provided)
  - `email` (nullable, valid email if provided)
  - `phone` (nullable)
  - `memberSince` (not null — manually informed, can be in the past)
  - Note: `registerDate` is **never in the request** — auto-set to today

- `TrainerUpdateRequest`:
  - Same fields as create, all optional (PATCH semantics)
  - `registerDate` is **never updatable**

- `TrainerResponse`:
  - `uuid`, `name`, `birthCountry`, `livingCountry`, `birthdate`, `email`, `phone`, `registerDate`, `memberSince`, `active`
  - Include computed `age` (nullable — only if birthdate is set)

- `TrainerSummaryResponse` (for list views):
  - `uuid`, `name`, `memberSince`, `active`

**Repository `TrainerRepository`:**
- `findByUuid(UUID uuid)` → `Optional<Trainer>`
- `findAllByActiveTrue(Pageable pageable)` → `Page<Trainer>`
- `findByNameContainingIgnoreCase(String name, Pageable pageable)` → `Page<Trainer>`

**Service `TrainerService`:**
- `createTrainer(TrainerCreateRequest)`:
  - Auto-set `registerDate = LocalDate.now()`
  - Validate `memberSince` is not in the future
  - Validate `birthdate` is in the past if provided
  - Return `Trainer`
- `getTrainerByUuid(UUID uuid)` → `Trainer`
- `getAllTrainers(Pageable pageable)` → `Page<Trainer>`
- `searchTrainers(String name, Pageable pageable)` → `Page<Trainer>`
- `updateTrainer(UUID uuid, TrainerUpdateRequest)` → `Trainer`
- `deactivateTrainer(UUID uuid)` → sets `active = false`
- `reactivateTrainer(UUID uuid)` → sets `active = true`

**Controller `TrainerController` — `/api/v1/trainers`:**
- `POST /api/v1/trainers` → `createTrainer()` — ADMIN
- `GET /api/v1/trainers` → `getAllTrainers()` — ADMIN (supports `?page=0&size=20&name=search`)
- `GET /api/v1/trainers/{uuid}` → `getTrainerByUuid()` — ADMIN
- `PUT /api/v1/trainers/{uuid}` → `updateTrainer()` — ADMIN
- `PATCH /api/v1/trainers/{uuid}/deactivate` → `deactivateTrainer()` — ADMIN
- `PATCH /api/v1/trainers/{uuid}/reactivate` → `reactivateTrainer()` — ADMIN

**Tests:**

Unit test `TrainerServiceTest`:
- `createTrainer_WithValidRequest_SetsRegisterDateToToday`
- `createTrainer_WithFutureMemberSince_ThrowsValidationException`
- `createTrainer_WithFutureBirthdate_ThrowsValidationException`
- `createTrainer_WithNullBirthdate_CreatesSuccessfully`
- `updateTrainer_WithValidRequest_UpdatesFields`
- `updateTrainer_RegisterDateIsNeverChanged`
- `deactivateTrainer_WhenActive_SetsActiveFalse`
- `reactivateTrainer_WhenInactive_SetsActiveTrue`
- `getAllTrainers_ReturnsPaginatedSummary`

MockMvc test `TrainerControllerTest`:
- `POST /api/v1/trainers` returns 201 with trainer uuid
- `POST /api/v1/trainers` returns 400 when name is blank
- `POST /api/v1/trainers` returns 400 when memberSince is in future
- `POST /api/v1/trainers` returns 400 when birthdate is in future
- `POST /api/v1/trainers` returns 403 without auth
- `GET /api/v1/trainers` returns 200 with paginated list
- `GET /api/v1/trainers/{uuid}` returns 200 with full trainer
- `GET /api/v1/trainers/{uuid}` returns 404 for unknown uuid
- `PUT /api/v1/trainers/{uuid}` returns 200 with updated trainer
- `PATCH /api/v1/trainers/{uuid}/deactivate` returns 200
- `PATCH /api/v1/trainers/{uuid}/reactivate` returns 200

---

### Frontend

**`src/api/trainers.js`:**
- `createTrainer(data)` → `POST /api/v1/trainers`
- `getAllTrainers(params)` → `GET /api/v1/trainers`
- `getTrainer(uuid)` → `GET /api/v1/trainers/{uuid}`
- `updateTrainer(uuid, data)` → `PUT /api/v1/trainers/{uuid}`
- `deactivateTrainer(uuid)` → `PATCH /api/v1/trainers/{uuid}/deactivate`
- `reactivateTrainer(uuid)` → `PATCH /api/v1/trainers/{uuid}/reactivate`

**Page `src/pages/TrainersPage.jsx`:**
- Protected route
- Table with columns: Name, Email, Member Since, Status (Active/Inactive badge), Actions
- Search bar (filters by name, debounced)
- Pagination controls
- "Add Trainer" button → opens modal/drawer with `TrainerForm`
- Row actions: View, Edit, Deactivate/Reactivate
- Inactive trainers shown with muted style

**Component `src/components/trainers/TrainerForm.jsx`:**
- Fields: Name (required), Email (optional), Phone (optional), Birth Country (optional), Living Country (optional), Birthdate (optional date picker), Member Since (required date picker)
- Member Since label: *"Date the trainer joined this club (can be in the past)"*
- Register Date: **not shown in form** — auto-set by backend

**Page `src/pages/TrainerDetailPage.jsx`:**
- Shows full trainer details
- Age computed and displayed if birthdate is set
- Edit button opens `TrainerForm` pre-filled
- Deactivate/Reactivate button with confirmation

**Playwright test `tests/e2e/trainers.spec.js`:**
- Admin logs in
- Navigates to Trainers page
- Clicks "Add Trainer"
- Fills form: Carlos Mendes, carlos@club.com, member since 2018-06-01
- Submits and asserts trainer appears in table
- Searches for "Carlos" and asserts only matching trainers shown
- Clicks trainer row → assert detail page shows correct data
- Clicks Deactivate → assert status badge changes to Inactive
