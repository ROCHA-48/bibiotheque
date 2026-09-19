# Project knowledge

This file gives Freebuff context about your project: goals, commands, conventions, and gotchas.

## Quickstart
- **One-command Docker**: `docker compose up --build` at root — Postgres 16 + backend (builds `bibliotheque-backend/Dockerfile`).
- **Local backend**: `cd bibliotheque-backend && ./mvnw spring-boot:run` (port 8080; spring-boot-devtools auto-restarts on classpath change).
- **Local frontend**: `cd bibliotheque-frontend && npm install && npm start` (port 4200; calls backend directly at `http://localhost:8080`).
- **Prereqs**: JDK 17–21, Node 20+, Docker Desktop (or local Postgres), Git. Verified passing: full backend suite + frontend build on JDK 21.0.12.

## Tests
- Backend: `cd bibliotheque-backend && ./mvnw test` — runs entirely on **H2 in-memory** (test-scope), no Postgres needed. Integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@WithMockUser` (roles as exact strings, e.g. `{"Admin"}`, `{"BIBLIOTHECAIRE"}`).
- Frontend: `cd bibliotheque-frontend && npm test -- --watch=false` — **ChromeHeadlessNoSandbox** launcher in `karma.conf.js` (no-sandbox, wide timeouts for CI/containers). One `*.spec.ts` per component, next to the component folder.

## Architecture
- **Stack**: Spring Boot 2.4.5 (pom targets Java 1.8) + Angular 14 + PostgreSQL 16 (runtime) / H2 (tests).
- **Backend** (8080): `bibliotheque-backend/src/main/java/com/ibizabroker/bibliotheque/`
  - `entity/` — Books, Users, Role, Borrow, Reservation (+ ReservationStatus enum, JwtRequest/Response); `JsonDataSerializer` renders dates as `dd-MM-yyyy`
  - `dao/` — Spring Data repo interfaces (BorrowRepository has `findByReturnDateIsNull()` used by the dashboard)
  - `controller/` — BooksController, AdminController, BorrowController, JwtController, ReservationController (`/api/reservations`), **DashboardController** (`/admin/dashboard`: stats, recent-activity, chart/borrows-by-month|reservation-status|books-by-genre)
  - `service/` — JwtService, ReservationService (reservation status lifecycle)
  - `configuration/` — WebSecurityConfiguration, JwtRequestFilter, JwtAuthenticationEntryPoint, JwtAccessDeniedHandler, WebCorsConfig, OpenApiConfig
  - `util/JwtUtil` — token creation/validation
- **Frontend** (4200): `bibliotheque-frontend/src/app/`
  - one folder per screen: login, registration, books-list, create-book, update-book, book-details, users-list, user-details, update-user, borrow-book, return-book, reservations, reservation-list, reservation-form, dashboard, sidebar, modals, toast
  - `_service/` — all HTTP clients (books, users, borrow, user-auth, reservation)
  - `_auth/` — auth.guard.ts (role check → `/forbidden`), auth.interceptor.ts (adds Bearer token; 401 → `/login`, 403 → `/forbidden`)
  - charts via `chart.js` 3.9.1 + `ng2-charts` 3.1.2 (`NgChartsModule`)
- **Infra**: `docker-compose.yml` (root) exposes Postgres on host port **5433** → container 5432.

## Conventions
- Backend: controller → service → dao → entity layering, but several controllers call repos directly (BorrowController, AdminController, DashboardController).
- Frontend: one folder = one screen; network code in `_service`.
- Security: `@PreAuthorize` on endpoints + role strings in frontend routing and guards. Exact role names matter: `Admin`, `BIBLIOTHECAIRE`, `ADHERENT` (legacy `User`/`Admin` still tolerated). Example: `/dashboard` is `Admin`-only end-to-end.
- API base URL hardcoded as `http://localhost:8080` in services — no `environment.ts`.
- Dates are serialized `dd-MM-yyyy` via `JsonDataSerializer` (`@JsonSerialize` on entity fields).
- Entities use explicit `@Table(name= "...")` (Books, Borrow, Reservation) or camelCase → snake_case naming.
- Swagger UI at `/swagger-ui.html` (springdoc-openapi-ui 1.5.13).

## Gotchas
- **Java/JDK**: pom targets Java 1.8 + Lombok 1.18.30; builds and tests pass on JDK 17–21. Lombok breaks on **JDK 22+** with `NoSuchFieldError: JCTree$JCImport`.
- **DB defaults are MySQL-flavored**: Postgres is the real DB, but `application.properties` and docker-compose default `DB_PASSWORD=mysql`, user `bibliotheque`, DB `bibliotheque`, local port 5433 (container overrides DB_HOST/DB_PORT via env).
- **README is stale**: still documents MySQL on 3306, a non-existent Dockerfile, and the `hibernate_sequence` table — trust the code, not the README.
- **Angular 14 on Node 22**: prints `Unsupported` but still builds.
- **No seed data**: `POST /admin/users` needs an existing token, so the first admin must be inserted by hand via SQL after the first backend start (BCrypt hash required).
- **Manual ID inserts don't advance Postgres identity sequences** (all entities use `GenerationType.IDENTITY`): after inserting rows with explicit IDs, run `SELECT setval(pg_get_serial_sequence('users','user_id'), (SELECT max(user_id) FROM users));` or the next insert may collide.