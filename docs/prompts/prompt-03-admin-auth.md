# Prompt 03 — Admin Registration & Authentication

## Goal
Implement admin registration, JWT-based login, and secured routes. Multiple admins can be registered. All protected endpoints require a valid JWT with ADMIN role.

---

## Instructions

Read `CLAUDE.md` before starting. Follow all conventions defined there.

---

### Backend

**Flyway migration `V3__admin.sql`:**
```sql
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed a default admin for development
INSERT INTO admin (uuid, name, email, username, password_hash)
VALUES (
    RANDOM_UUID(),
    'Admin',
    'admin@clubmanager.com',
    'admin',
    -- BCrypt hash of 'admin123'
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);
```

**Entity `Admin`:**
- Extends `AbstractEntity`
- Fields: `name`, `email`, `username`, `passwordHash`, `createdAt`

**DTOs:**
- `AdminRegisterRequest`: `name` (not blank), `email` (valid email), `username` (not blank, 3–50 chars), `password` (min 6 chars)
- `AdminResponse`: `uuid`, `name`, `email`, `username`, `createdAt`
- `LoginRequest`: `username` (not blank), `password` (not blank)
- `LoginResponse`: `token`, `adminUuid`, `name`

**Repository `AdminRepository`:**
- `findByUsername(String username)` → `Optional<Admin>`
- `findByEmail(String email)` → `Optional<Admin>`
- `findByUuid(UUID uuid)` → `Optional<Admin>`
- `existsByUsername(String username)` → `boolean`
- `existsByEmail(String email)` → `boolean`

**Service `AdminService`:**
- `register(AdminRegisterRequest)` → validates uniqueness of username and email, encodes password with BCrypt, saves, returns `AdminResponse`
- `login(LoginRequest)` → validates credentials, generates JWT, returns `LoginResponse`
- `getAllAdmins()` → returns list of `AdminResponse`
- `getAdminByUuid(UUID uuid)` → returns `AdminResponse`
- `deleteAdmin(UUID uuid)` → only if more than 1 admin exists (cannot delete last admin)

**JWT implementation (`config/`):**
- `JwtService`: `generateToken(username)`, `validateToken(token)`, `extractUsername(token)`
- Token includes: `sub` (username), `uuid` (admin uuid), `role` (ADMIN), `iat`, `exp`
- Expiration configurable via `application.yml` (default 24h)

**`SecurityConfig`:**
- Stateless session
- Public endpoints: `POST /api/v1/auth/login`, `GET /api/v1/club`, `/h2-console/**`, `/actuator/health`
- All other endpoints require `ROLE_ADMIN`
- JWT filter: `OncePerRequestFilter` that extracts and validates Bearer token

**Controller `AuthController` — `/api/v1/auth`:**
- `POST /api/v1/auth/login` → `login()` — public
- `POST /api/v1/auth/register` → `register()` — requires ADMIN (only existing admins can register new ones)

**Controller `AdminController` — `/api/v1/admins`:**
- `GET /api/v1/admins` → `getAllAdmins()` — requires ADMIN
- `GET /api/v1/admins/{uuid}` → `getAdminByUuid()` — requires ADMIN
- `DELETE /api/v1/admins/{uuid}` → `deleteAdmin()` — requires ADMIN

**Tests:**

Unit test `AdminServiceTest`:
- `register_WithValidRequest_ReturnsAdminResponse`
- `register_WithDuplicateUsername_ThrowsException`
- `register_WithDuplicateEmail_ThrowsException`
- `login_WithValidCredentials_ReturnsTokenResponse`
- `login_WithInvalidPassword_ThrowsException`
- `deleteAdmin_WhenOnlyOneAdmin_ThrowsException`
- `deleteAdmin_WhenMultipleAdmins_DeletesSuccessfully`

MockMvc test `AuthControllerTest`:
- `POST /api/v1/auth/login` returns 200 with token for valid credentials
- `POST /api/v1/auth/login` returns 401 for invalid credentials
- `POST /api/v1/auth/login` returns 400 for missing fields
- `POST /api/v1/auth/register` returns 201 when authenticated
- `POST /api/v1/auth/register` returns 403 when not authenticated
- `POST /api/v1/auth/register` returns 400 for duplicate username

MockMvc test `AdminControllerTest`:
- `GET /api/v1/admins` returns 200 with list
- `GET /api/v1/admins` returns 403 without token
- `DELETE /api/v1/admins/{uuid}` returns 204 when multiple admins exist
- `DELETE /api/v1/admins/{uuid}` returns 400 when last admin

---

### Frontend

**`src/api/auth.js`:**
- `login(username, password)` → `POST /api/v1/auth/login`
- `registerAdmin(data)` → `POST /api/v1/auth/register`

**`src/api/admins.js`:**
- `getAllAdmins()` → `GET /api/v1/admins`
- `deleteAdmin(uuid)` → `DELETE /api/v1/admins/{uuid}`

**Update `AuthContext.jsx`:**
- `login(username, password)`: call API, store token in memory state, store `adminUuid` and `name`
- `logout()`: clear all state
- `getToken()`: returns current token
- `isAuthenticated()`: returns boolean

**Page `src/pages/LoginPage.jsx`:**
- Username and password fields
- On submit, call `login()` from AuthContext
- On success, redirect to `/dashboard`
- Show error message on failure
- Apply club colours (already loaded by ClubContext)

**Page `src/pages/AdminsPage.jsx`:**
- Protected route
- List all admins (name, email, username, createdAt)
- Button to register a new admin (modal form: name, email, username, password)
- Delete button per admin (disabled if only 1 admin remains)
- Confirm dialog before delete

**Component `src/components/ProtectedRoute.jsx`:**
- Wraps routes that require authentication
- Redirects to `/login` if not authenticated

**Playwright test `tests/e2e/auth.spec.js`:**
- Navigate to `/login`
- Fill username `admin` and password `admin123`
- Assert redirect to `/dashboard`
- Assert club name is visible in navbar
- Navigate to `/login` again while authenticated → assert redirect to `/dashboard`
- Click logout → assert redirect to `/login`
