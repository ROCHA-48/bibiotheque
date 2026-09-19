# Project knowledge

## What this is

Full-stack **French-language library management app** ("Bibliothèque") with roles:
- **Admin** — CRUD books and users.
- **User / ADHERENT** — borrow / return / reserve books.
- **BIBLIOTHECAIRE** (librarian) — manage reservations (confirm, delete), manage users.

Stack (legacy, by design — see "Known friction" below):
- Backend: **Spring Boot 2.4.5**, Java 1.8 source, **Lombok 1.18.30**, **JJWT 0.9.1**, JWT + BCrypt, Spring Security.
- Frontend: **Angular 14**, Bootstrap 5, JWT stored in `localStorage`.
- DB: **PostgreSQL 16** at runtime; **H2** for tests (`pom.xml` test scope).
- Infra: `docker-compose.yml` starts `db` (Postgres) + `backend` (built from `bibliotheque-backend/Dockerfile`). No Compose service for the frontend.

## Where key code lives

- Backend: `bibliotheque-backend/src/main/java/com/ibizabroker/bibliotheque/`
  - `entity/` — JPA entities (Books, Users, Role, Borrow, **Reservation + ReservationStatus enum**, JwtRequest/Response, JsonDataSerializer).
  - `dao/` — Spring Data JPA repos (BooksRepository, UsersRepository, BorrowRepository, RoleRepository, **ReservationRepository**).
  - `controller/` — HTTP entrypoints:
    - `BooksController` → `/admin/books`
    - `AdminController` → `/admin/users`
    - `BorrowController` → `/borrow`
    - `JwtController` → `/authenticate`
    - `ReservationController` → `/api/reservations` (create, list with optional `?status=`, get by id, `PATCH /{id}/annuler`, delete for librarian/admin)
  - `service/` — `JwtService` (login), `ReservationService` (all reservation business rules).
  - `configuration/` — `WebSecurityConfiguration`, `JwtRequestFilter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`, `OpenApiConfig`, **`WebCorsConfig`** (the former `CorsConfiguration` was renamed).
  - `util/JwtUtil` — token creation/validation.
  - `exceptions/NotFoundException` → HTTP 404.
  - `src/main/resources/application.properties` — port 8080, DB URL, JWT secret.
- Frontend: `bibliotheque-frontend/src/app/`
  - `app-routing.module.ts` routes + role guards.
  - `_service/` — HTTP clients: books, users, borrow, user-auth, **reservation.service, reservation-books.service, reservation-users.service**, plus UI helpers (modal.service, toast.service).
  - `_auth/` — `auth.guard.ts`, `auth.interceptor.ts` (attaches `Bearer <token>`, redirects on 401/403).
  - `_model/` — TS types (books, users, borrow, **reservation**).
  - One folder per screen (15+ components): `login`, `books`, `create-book`, `borrow-book`, `return-book`, `users`, `admin`, **`reservations`, `reservation-list`, `reservation-form`**, etc.
  - API base URL `http://localhost:8080` is hardcoded in the service files — **not** in `environment.ts`.
- Infra: `docker-compose.yml` (root), `bibliotheque-backend/Dockerfile` (multi-stage Maven → JRE Alpine, non-root user `spring`).
- Test tooling: `bibliotheque-backend/generate-test-report.py` — turns surefire XML into a visual HTML report (`target/test-report.html`).

## How to run (dev)

Prereqs (from README, somewhat dated): JDK 17+, Node 20+, npm, Docker Desktop running, Git.

**One-command Docker path:**
```bash
docker compose up --build
```
Backend built from `./bibliotheque-backend`, talks to Postgres service `db`. API on `http://localhost:8080`.

**Local backend (when not using Compose):**
```bash
cd bibliotheque-backend
./mvnw spring-boot:run
```
First run with `spring.jpa.hibernate.ddl-auto=update` creates tables. DB expected at `jdbc:postgresql://localhost:5433/bibliotheque` (user `bibliotheque` / password `mysql` by default; adjustable via env or `application.properties`).

**Local frontend:**
```bash
cd bibliotheque-frontend
npm install
npm start
```
Serves on `http://localhost:4200`.

## Tests

- Backend: `./mvnw test` from `bibliotheque-backend` (runs on H2, no Postgres needed). Suites:
  - `BibliothequeApplicationTests` — context boots.
  - `ReservationServiceUnitTest` — reservation business rules, mocked repos.
  - `ReservationSecurityIntegrationTest` — role-based access on `/api/reservations`.
  - `AdminUserDeletionIntegrationTest` — admin user deletion behavior.
  - `SecurityErrorResponsesTest` — 401/403 JSON error bodies from JWT handlers.
  - Optional pretty report: `python3 generate-test-report.py` after `mvn test`.
- Frontend: Karma/Jasmine, `npm test` → `ng test` (run in Chrome). Spec files alongside components and services.
- **No lint tooling configured** on either side (no Checkstyle/Spotless in `pom.xml`, no eslint in `package.json`).

## Creating the first account

No seed data; `POST /admin/users` requires a JWT. Bootstrap the first admin in SQL **after** the backend has started once (tables must exist). Password must be BCrypt — the README ships a known hash for `admin123`. Then:
```bash
curl -X POST http://localhost:8080/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Use the returned `jwtToken` in `Authorization: Bearer <token>` for admin calls.

## Notable conventions and patterns

- Backend layering: `controller` receives, `service` decides, `dao` persists, `entity` represents.
- Frontend: one folder = one screen; network code lives in `_service`.
- Security: JWT filter runs before controllers; `WebSecurityConfiguration` declares a `BCryptPasswordEncoder`; `@PreAuthorize` on admin/reservation endpoints. Roles use both legacy names (`Admin`, `User`) and new ones (`BIBLIOTHECAIRE`, `ADHERENT`) — `hasAnyRole(...)` lists cover both, keep that pairing when adding endpoints.
- Missing role → 403 with a JSON error body (`JwtAccessDeniedHandler`), 401 via `JwtAuthenticationEntryPoint`; frontend redirects to `/forbidden` / `/login`.
- CORS: `WebCorsConfig` allows the frontend port (4200); reservation controller also declares `@CrossOrigin("http://localhost:4200/")`.
- Dates serialized as `dd-MM-yyyy` via `JsonDataSerializer`.
- SpringDoc OpenAPI UI present (`springdoc-openapi-ui 1.5.13`); `ReservationController` documents `@SecurityRequirement(name = "Bearer")`.

## Known friction / gotchas

- **Java version mismatch**: `pom.xml` declares `java.version=1.8` and uses Spring Boot 2.4.5 + Lombok 1.18.30. On JDK 21 the Maven build fails with `NoSuchFieldError ... JCTree$JCImport`. Known constraint, not a transient bug.
- **Postgres vs MySQL**: README talks about MySQL (user `root` / `mysql`, port 3306) but the actual `pom.xml` depends on `postgresql`, `docker-compose.yml` uses `postgres:16-alpine`, and `application.properties` uses `jdbc:postgresql://...`. Treat Postgres as canonical.
- **Angular 14 on Node 22**: `ng version` flags Node as unsupported; builds usually pass anyway, but it's out of official support.
- **Hardcoded API URL**: frontend services hit `http://localhost:8080` directly, so dev only works with the backend on that port/machine.
- **No Docker Compose for frontend**: only backend + db are containerized.
- **Table names**: Hibernate applies `camelCase → snake_case`; verify with `\dt` / `\d table` in psql rather than assuming exact names.
- **`hibernate_sequence`**: if you insert IDs manually, bump `hibernate_sequence.next_val` past your hand-assigned IDs to avoid collisions on later app-created rows.

## Repo context for this session

Branch: `feature/reservation-securite-wilson-rocha` — adds the reservation feature end-to-end (entity/status, repository, service, controller, frontend screens/services) with security hardening (role checks, JSON 401/403 handlers) and the test suites above. See `PULL_REQUEST.md` for the PR description.
