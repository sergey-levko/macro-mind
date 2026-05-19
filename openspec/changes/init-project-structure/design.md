## Context

The MacroMind repository contains only the PDF concept document and the OpenSpec config. No runnable code exists. This design establishes the foundational repository layout, tooling wiring, and inter-module contracts that every subsequent vertical slice will depend on.

## Goals / Non-Goals

**Goals:**
- Define the repo directory layout (`backend/`, `frontend/`, `docker-compose.yml` at root)
- Establish a working Spring Boot 3 app that connects to a Dockerised PostgreSQL 16 instance
- Establish a working Vite + React + TypeScript app with TailwindCSS and Recharts
- Configure Liquibase with an empty baseline changeset so future migration tasks have a stable starting point
- Add a `GET /api/v1/health` smoke-test endpoint reachable from the frontend dev proxy
- Wire the Spring AI dependency so future AI slices only need to add service logic

**Non-Goals:**
- Domain entities, JPA repositories, or business logic
- Authentication / session management
- CI/CD pipeline or cloud deployment
- USDA API integration
- Any UI screens beyond a "Hello MacroMind" placeholder

## Decisions

### D1 — Mono-repo with `backend/` and `frontend/` top-level directories
**Decision:** Keep both projects in one git repo under separate subdirectories rather than separate repos.
**Rationale:** Simplifies coordinated commits across stack layers (single PR covers full vertical slice). Acceptable for a PoC; can be split later if team boundaries require it.
**Alternative considered:** Separate repos — rejected because it adds overhead (cross-repo PRs, version pinning) with no benefit at this stage.

### D2 — Maven for backend, Vite for frontend
**Decision:** Use Maven (not Gradle) for the Spring Boot project; use Vite (not CRA) for the React project.
**Rationale:** Maven is the Spring Boot ecosystem default; Vite is the current standard for React+TypeScript with fast HMR. Both are well-supported in Spring Initializr and the React ecosystem respectively.

### D3 — Docker Compose manages PostgreSQL only
**Decision:** Docker Compose runs PostgreSQL 16 only; backend and frontend run on the host JVM / Node process during development.
**Rationale:** Keeps the dev loop fast (no rebuilding app containers on every code change). Testcontainers spins up isolated DB instances for integration tests independently of the compose file.
**Alternative considered:** Full-stack Docker Compose — rejected because it slows the inner dev loop significantly.

### D4 — Liquibase changelog root at `backend/src/main/resources/db/changelog/`
**Decision:** Single master changelog file `db.changelog-master.yaml` includes individual changeset files.
**Rationale:** Matches Liquibase recommended layout and Spring Boot auto-configuration defaults (`spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml`). Keeps changesets isolated per feature.

### D5 — Spring AI wired as a dependency with Claude model configured via properties
**Decision:** Add `spring-ai-anthropic-spring-boot-starter` to `pom.xml`; configure `spring.ai.anthropic.api-key` via environment variable; no AI service beans in the skeleton.
**Rationale:** Validates that Spring AI resolves correctly in the build and that the API key wiring works before any AI feature slice needs it. Avoids surprises at integration time.

### D6 — Frontend dev proxy routes `/api` to `localhost:8080`
**Decision:** Configure Vite `proxy` in `vite.config.ts` to forward `/api/**` to `http://localhost:8080`.
**Rationale:** Avoids CORS configuration in the skeleton; keeps the frontend calling a relative path (`/api/v1/health`) that works identically in dev and production.

## Risks / Trade-offs

- **[Risk] Spring AI version incompatibility with Spring Boot 3.x** → Mitigation: pin `spring-ai.version` in Maven properties to the latest stable release tested against Spring Boot 3.3+; document the pinned version.
- **[Risk] Liquibase baseline changeset causes conflicts if team runs against an existing DB** → Mitigation: Docker Compose always starts a fresh named volume in local dev; Testcontainers always provides a clean container.
- **[Risk] Mono-repo becomes unwieldy as team grows** → Mitigation: directory layout is clean from day one; splitting is a 1-day mechanical task if needed.

## Migration Plan

1. Merge this scaffold as the first commit on `master`
2. All subsequent vertical-slice PRs branch off `master` and add within `backend/` or `frontend/`
3. No rollback needed — this is the initial state

## Open Questions

- *(none blocking)* Java package root: use `com.epam.macromind` — pending team confirmation, but not blocking scaffold creation.
