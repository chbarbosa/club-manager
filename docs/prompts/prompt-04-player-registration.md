# Prompt 04 — Player Registration

## Goal
Implement full player registration: create, list, view, update, and deactivate players. Players have two dates (registerDate auto-set, memberSince manual), countries, birthdate, gender, and an optional registration number.

---

## Instructions

Read `CLAUDE.md` before starting. Follow all conventions defined there.

---

### Backend

**Flyway migration `V4__player.sql`:**
```sql
CREATE TABLE player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    birth_country VARCHAR(100) NOT NULL,
    living_country VARCHAR(100) NOT NULL,
    birthdate DATE NOT NULL,
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    registration_number VARCHAR(50),
    register_date DATE NOT NULL,
    member_since DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Entity `Player`:**
- Extends `AbstractEntity`
- Fields: `name`, `birthCountry`, `livingCountry`, `birthdate` (LocalDate), `gender` (enum: M/F), `registrationNumber` (nullable), `registerDate` (LocalDate), `memberSince` (LocalDate), `active` (boolean, default true)

**Enum `Gender`:** `M`, `F`

**DTOs:**
- `PlayerCreateRequest`:
  - `name` (not blank)
  - `birthCountry` (not blank)
  - `livingCountry` (not blank)
  - `birthdate` (not null, must be in the past)
  - `gender` (not null, M or F)
  - `registrationNumber` (nullable)
  - `memberSince` (not null — manually informed, can be in the past)
  - Note: `registerDate` is **never in the request** — auto-set to today on creation

- `PlayerUpdateRequest`:
  - Same fields as create, all optional (PATCH semantics)
  - `registrationNumber` updatable
  - `memberSince` updatable
  - `registerDate` is **never updatable**

- `PlayerResponse`:
  - `uuid`, `name`, `birthCountry`, `livingCountry`, `birthdate`, `gender`, `registrationNumber`, `registerDate`, `memberSince`, `active`
  - Include computed field `age` (calculated from birthdate)

- `PlayerSummaryResponse` (for list views):
  - `uuid`, `name`, `birthdate`, `age`, `gender`, `memberSince`, `active`

**Repository `PlayerRepository`:**
- `findByUuid(UUID uuid)` → `Optional<Player>`
- `findAllByActiveTrue(Pageable pageable)` → `Page<Player>`
- `findAllByGender(Gender gender, Pageable pageable)` → `Page<Player>`
- `findByNameContainingIgnoreCase(String name, Pageable pageable)` → `Page<Player>`
- `existsByRegistrationNumber(String registrationNumber)` → `boolean`

**Service `PlayerService`:**
- `createPlayer(PlayerCreateRequest)`:
  - Auto-set `registerDate = LocalDate.now()`
  - Validate `memberSince` is not in the future
  - Validate `birthdate` is in the past
  - Validate `registrationNumber` uniqueness if provided
  - Return `PlayerResponse`
- `getPlayerByUuid(UUID uuid)` → `PlayerResponse`
- `getAllPlayers(Pageable pageable)` → `Page<PlayerSummaryResponse>`
- `searchPlayers(String name, Pageable pageable)` → `Page<PlayerSummaryResponse>`
- `updatePlayer(UUID uuid, PlayerUpdateRequest)` → `PlayerResponse`
- `deactivatePlayer(UUID uuid)` → sets `active = false` (soft delete)
- `reactivatePlayer(UUID uuid)` → sets `active = true`

**Controller `PlayerController` — `/api/v1/players`:**
- `POST /api/v1/players` → `createPlayer()` — ADMIN
- `GET /api/v1/players` → `getAllPlayers()` — ADMIN (supports `?page=0&size=20&name=search`)
- `GET /api/v1/players/{uuid}` → `getPlayerByUuid()` — ADMIN
- `PUT /api/v1/players/{uuid}` → `updatePlayer()` — ADMIN
- `PATCH /api/v1/players/{uuid}/deactivate` → `deactivatePlayer()` — ADMIN
- `PATCH /api/v1/players/{uuid}/reactivate` → `reactivatePlayer()` — ADMIN

**Tests:**

Unit test `PlayerServiceTest`:
- `createPlayer_WithValidRequest_SetsRegisterDateToToday`
- `createPlayer_WithFutureMemberSince_ThrowsValidationException`
- `createPlayer_WithFutureBirthdate_ThrowsValidationException`
- `createPlayer_WithDuplicateRegistrationNumber_ThrowsException`
- `updatePlayer_WithValidRequest_UpdatesFields`
- `updatePlayer_RegisterDateIsNeverChanged`
- `deactivatePlayer_WhenActive_SetsActiveFalse`
- `reactivatePlayer_WhenInactive_SetsActiveTrue`
- `getAllPlayers_ReturnsPaginatedSummary`
- `searchPlayers_ByName_ReturnsMathcingResults`

MockMvc test `PlayerControllerTest`:
- `POST /api/v1/players` returns 201 with player uuid
- `POST /api/v1/players` returns 400 when name is blank
- `POST /api/v1/players` returns 400 when birthdate is in future
- `POST /api/v1/players` returns 400 when memberSince is in future
- `POST /api/v1/players` returns 403 without auth
- `GET /api/v1/players` returns 200 with paginated list
- `GET /api/v1/players/{uuid}` returns 200 with full player
- `GET /api/v1/players/{uuid}` returns 404 for unknown uuid
- `PUT /api/v1/players/{uuid}` returns 200 with updated player
- `PATCH /api/v1/players/{uuid}/deactivate` returns 200
- `PATCH /api/v1/players/{uuid}/reactivate` returns 200

---

### Frontend

**`src/api/players.js`:**
- `createPlayer(data)` → `POST /api/v1/players`
- `getAllPlayers(params)` → `GET /api/v1/players` (with pagination + search)
- `getPlayer(uuid)` → `GET /api/v1/players/{uuid}`
- `updatePlayer(uuid, data)` → `PUT /api/v1/players/{uuid}`
- `deactivatePlayer(uuid)` → `PATCH /api/v1/players/{uuid}/deactivate`
- `reactivatePlayer(uuid)` → `PATCH /api/v1/players/{uuid}/reactivate`

**Page `src/pages/PlayersPage.jsx`:**
- Protected route
- Table with columns: Name, Age, Gender, Member Since, Status (Active/Inactive badge), Actions
- Search bar (filters by name, debounced)
- Pagination controls
- "Add Player" button → opens modal/drawer with `PlayerForm`
- Row actions: View, Edit, Deactivate/Reactivate
- Inactive players shown with muted style

**Component `src/components/players/PlayerForm.jsx`:**
- Fields: Name, Birth Country, Living Country, Birthdate (date picker), Gender (M/F select), Registration Number (optional), Member Since (date picker)
- Member Since: label says *"Date the player started at this club (can be in the past)"*
- Register Date: **not shown in form** — auto-set by backend
- Submit calls create or update depending on context

**Page `src/pages/PlayerDetailPage.jsx`:**
- Shows full player details
- Age computed and displayed
- Edit button opens `PlayerForm` pre-filled
- Deactivate/Reactivate button with confirmation

**Playwright test `tests/e2e/players.spec.js`:**
- Admin logs in
- Navigates to Players page
- Clicks "Add Player"
- Fills form: João Silva, Brazil, Brazil, 2005-03-15, M, member since 2020-01-01
- Submits and asserts player appears in table
- Searches for "João" and asserts only matching players shown
- Clicks player row → assert detail page shows correct data
- Clicks Deactivate → assert status badge changes to Inactive
