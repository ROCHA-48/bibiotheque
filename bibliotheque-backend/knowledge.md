# knowledge.md

## What this project is
"Bibliotheque" — a Library Management System backend built with Spring Boot 2.4.5 (Maven, Java 8 target). Handles books, borrows, and reservations with JWT-based auth and role-based access (Admin, User, BIBLIOTHECAIRE, ADHERENT). PostgreSQL in production/dev; H2 in-memory for tests. Current branch (`feature/reservation-securite-wilson-rocha`) focuses on reservation security.

## Commands
- Run dev server: `./mvnw spring-boot:run` (serves on port 8080 by default)
- Run all tests: `./mvnw test` (Surefire reports land in `target/surefire-reports`)
- Run one test class: `./mvnw test -Dtest=ReservationServiceUnitTest`
- Build JAR (skipping tests): `./mvnw clean package -DskipTests`
- Generate HTML test report: `python3 generate-test-report.py` → `target/test-report.html`
- Docker build: `docker build -t bibliotheque .` (multi-stage; runtime is JRE 21 Alpine, non-root user, port 8080)

## Architecture
Base package: `com.ibizabroker.bibliotheque` (under `src/main/java/com/ibizabroker/bibliotheque/`)
- `controller/` — REST endpoints: `JwtController` (auth), `BooksController`, `BorrowController`, `ReservationController`, `AdminController`, `DashboardController`
- `service/` — business logic (e.g. `ReservationService`)
- `dao/` — Spring Data JPA repositories (`BooksRepository`, `BorrowRepository`, `ReservationRepository`, `RoleRepository`, ...)
- `entity/` — JPA entities: `Books`, `Borrow`, `Reservation` (+ `ReservationStatus`), `Users`, `Role`, JWT request/response DTOs, `JsonDataSerializer`
- `configuration/` — Spring Security config (`WebSecurityConfiguration`, `JwtRequestFilter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`), CORS (`WebCorsConfig`), OpenAPI (`OpenApiConfig`)
- `util/` — `JwtUtil` (token generation/validation)
- `exceptions/` — custom exception classes

Data flow: HTTP request → `JwtRequestFilter` (validates JWT, sets SecurityContext) → controller → service → repository → PostgreSQL.

Config: `src/main/resources/application.properties` externalizes everything via env vars — `DB_HOST`/`DB_PORT` (default 5433)/`DB_NAME`/`DB_USER`/`DB_PASSWORD`, `JWT_SECRET`, `SERVER_PORT`, `SPRING_JPA_HIBERNATE_DDL_AUTO` (default `update`), `JPA_SHOW_SQL`.

Seed data: `sql/test-data.sql` inserts roles, users (admin/admin123, jean/user123, bibliothecaire/biblio123, adherent1/adherent123, adherent2/adherent456), and books — useful for manual testing.

Tests: `src/test/java/...` — unit tests (`ReservationServiceUnitTest`) and integration tests (`ReservationSecurityIntegrationTest`, `AdminUserDeletionIntegrationTest`, `DashboardControllerTest`, ...) using H2 + spring-security-test.

## Conventions & gotchas
- Lombok is used for entities/DTOs (`@Data`, `@Getter`/`@Setter`...) — excluded from the repackaged JAR; run annotation processing enabled in the IDE.
- Tech stack pinned: Spring Boot 2.4.5, jjwt 0.9.1, springdoc-openapi-ui 1.5.13 (Swagger UI available in dev).
- ⚠️ `pom.xml` targets Java 1.8, but the Dockerfile builds/runs on JDK 21 — keep code compatible with both (avoid APIs removed after Java 8 when editing).
- JWT secret defaults to `learn_programming_yourself` — always set `JWT_SECRET` outside local dev.
- Hibernate `ddl-auto=update` by default; schema changes are applied automatically, so be deliberate with entity changes.
- Comments and test data are in French; keep that style when editing existing files.
- Use the Maven wrapper (`./mvnw`), not a system Maven, to match the pinned plugin versions.
