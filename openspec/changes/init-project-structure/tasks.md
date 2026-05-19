## 1. Root Scaffold & Docker Compose

- [x] 1.1 Create root `.gitignore` covering Java, Maven, Node, IDE files, and `.env` secrets
- [x] 1.2 Create `docker-compose.yml` with PostgreSQL 16 service (named volume, port 5432, env vars for DB name/user/password)
- [x] 1.3 Create root `README.md` with local dev quick-start (prerequisites, start DB, run backend, run frontend, verify health endpoint)

## 2. Backend — Spring Boot Project Initialisation

- [x] 2.1 Generate `backend/` Maven project via Spring Initializr (Java 21, Spring Boot 3, dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Liquibase Migration, Spring AI Anthropic)
- [x] 2.2 Configure `backend/src/main/resources/application.yml` with datasource pointing to Docker Compose DB (host/port/name/user/password via env vars), JPA settings, and Liquibase enabled
- [x] 2.3 Configure `spring.ai.anthropic.api-key` to read from `ANTHROPIC_API_KEY` env var; add `spring.ai.anthropic.chat.options.model` placeholder in `application.yml`

## 3. Backend — Liquibase Baseline

- [x] 3.1 Create `backend/src/main/resources/db/changelog/db.changelog-master.yaml` master changelog file
- [x] 3.2 Create `backend/src/main/resources/db/changelog/changes/0001-baseline.yaml` empty baseline changeset (no DDL — establishes Liquibase tracking only)

## 4. Backend — Health Endpoint

- [x] 4.1 Implement `HealthController` with `GET /api/v1/health` returning `{"status":"UP"}` (HTTP 200)
- [x] 4.2 Write unit test for `HealthController` using `@WebMvcTest` (JUnit 5)
- [x] 4.3 Write integration test verifying the health endpoint against a Testcontainers PostgreSQL instance

## 5. Frontend — React TypeScript Project Initialisation

- [x] 5.1 Scaffold `frontend/` with Vite (React + TypeScript template); install TailwindCSS (with PostCSS config) and Recharts
- [x] 5.2 Configure TailwindCSS (`tailwind.config.js`, `postcss.config.js`, import in `index.css`)
- [x] 5.3 Configure Vite dev proxy in `vite.config.ts` to forward `/api/**` to `http://localhost:8080`

## 6. Frontend — Placeholder Home Page

- [x] 6.1 Replace default `App.tsx` with a placeholder page displaying "MacroMind" heading and subtitle; apply basic TailwindCSS styling
- [x] 6.2 Verify `npm run build` produces no TypeScript or lint errors

## 7. Verification & Commit

- [ ] 7.1 Run the full stack locally (Docker Compose DB + backend + frontend) and confirm `GET /api/v1/health` returns `{"status":"UP"}` via browser and via frontend proxy (`/api/v1/health`)
- [x] 7.2 Run `./mvnw test` in `backend/` and confirm all tests pass (unit + integration)
- [ ] 7.3 Commit all changes atomically with message `chore: init project structure (backend skeleton, frontend skeleton, docker-compose)`
