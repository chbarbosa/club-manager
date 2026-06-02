# Prompt 02 — Club Setup

## Goal
Implement the club setup feature: a single-row configuration table storing the club's name, description, and two brand colours, plus a setup table for configurable lookup values. The frontend must load these colours on startup and apply them as CSS variables.

---

## Instructions

Read `CLAUDE.md` before starting. Follow all conventions defined there.

---

### Backend

**Flyway migration `V2__club_setup.sql`:**
```sql
CREATE TABLE club (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    colour1 VARCHAR(7) NOT NULL DEFAULT '#2d2d2d',
    colour2 VARCHAR(7) NOT NULL DEFAULT '#f0f0f0'
);

CREATE TABLE club_setup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    json_data TEXT NOT NULL
);

-- Seed default club row
INSERT INTO club (uuid, name, description, colour1, colour2)
VALUES (RANDOM_UUID(), 'My Club', 'Configure your club.', '#2d2d2d', '#f0f0f0');

-- Seed default setup values
INSERT INTO club_setup (uuid, type, json_data)
VALUES (RANDOM_UUID(), 'EVALUATION_LEVEL', '["Debutant", "Advanced", "Skilled"]');

INSERT INTO club_setup (uuid, type, json_data)
VALUES (RANDOM_UUID(), 'FEDERATIVE_UNIT', '["Province 1", "Province 2"]');
```

**Entity `Club`:**
- Extends `AbstractEntity`
- Fields: `name`, `description`, `colour1`, `colour2`

**Entity `ClubSetup`:**
- Extends `AbstractEntity`
- Fields: `type` (String), `jsonData` (String, column `json_data`)

**DTOs:**
- `ClubResponse`: `uuid`, `name`, `description`, `colour1`, `colour2`
- `ClubUpdateRequest`: `name`, `description`, `colour1`, `colour2` (all validated: name not blank, colours match hex pattern `^#[0-9A-Fa-f]{6}$`)
- `ClubSetupResponse`: `uuid`, `type`, `jsonData`
- `ClubSetupUpdateRequest`: `type`, `jsonData` (validated: type not blank, jsonData valid JSON string)

**Repository:**
- `ClubRepository` extends `JpaRepository<Club, Long>` with `findByUuid(UUID uuid)`
- `ClubSetupRepository` extends `JpaRepository<ClubSetup, Long>` with `findByType(String type)`, `findByUuid(UUID uuid)`

**Service `ClubService`:**
- `getClub()` → returns the single club row as `ClubResponse`
- `updateClub(ClubUpdateRequest)` → updates the single club row
- `getAllSetup()` → returns all `ClubSetupResponse`
- `getSetupByType(String type)` → returns single `ClubSetupResponse`
- `updateSetup(UUID uuid, ClubSetupUpdateRequest)` → updates a setup entry

**Controller `ClubController` — `/api/v1/club`:**
- `GET /api/v1/club` → `getClub()` — **public endpoint, no auth required** (needed by frontend on startup)
- `PUT /api/v1/club` → `updateClub()` — requires ADMIN role
- `GET /api/v1/club/setup` → `getAllSetup()` — requires ADMIN role
- `GET /api/v1/club/setup/{type}` → `getSetupByType()` — requires ADMIN role
- `PUT /api/v1/club/setup/{uuid}` → `updateSetup()` — requires ADMIN role

**Tests:**

Unit test `ClubServiceTest`:
- `getClub_WhenClubExists_ReturnsClubResponse`
- `updateClub_WithValidRequest_UpdatesAndReturnsResponse`
- `updateClub_WithInvalidColour_ThrowsValidationException`
- `getSetupByType_WhenExists_ReturnsSetupResponse`
- `updateSetup_WithValidJson_UpdatesSuccessfully`

MockMvc test `ClubControllerTest`:
- `GET /api/v1/club` returns 200 with club data (no auth)
- `PUT /api/v1/club` returns 200 when authenticated as ADMIN
- `PUT /api/v1/club` returns 403 when not authenticated
- `PUT /api/v1/club` returns 400 when colour format is invalid
- `GET /api/v1/club/setup` returns 200 with all setup entries
- `PUT /api/v1/club/setup/{uuid}` returns 200 with updated setup

---

### Frontend

**`src/api/club.js`:**
- `getClub()` → `GET /api/v1/club`
- `updateClub(data)` → `PUT /api/v1/club`
- `getAllSetup()` → `GET /api/v1/club/setup`
- `updateSetup(uuid, data)` → `PUT /api/v1/club/setup/{uuid}`

**Update `ClubContext.jsx`:**
- On mount, call `getClub()`
- Apply `colour1` → `--club-primary`, `colour2` → `--club-secondary` on `document.documentElement.style`
- Store club name and description in context for use in navbar/header

**Page `src/pages/ClubSettingsPage.jsx`:**
- Protected route (admin only)
- Form to edit: name, description, colour1 (colour picker), colour2 (colour picker)
- Live preview: show a mini navbar and button using the selected colours before saving
- On save, call `updateClub()` and re-apply CSS variables immediately
- Section below for managing setup entries (EVALUATION_LEVEL, FEDERATIVE_UNIT)
- Each setup entry shows as an editable tag list (add/remove values, save as JSON array)

**Playwright test `tests/e2e/club-settings.spec.js`:**
- Admin logs in
- Navigates to club settings
- Changes club name and saves
- Asserts new name appears in the navbar
- Changes colour1 and saves
- Asserts CSS variable `--club-primary` has the new value
