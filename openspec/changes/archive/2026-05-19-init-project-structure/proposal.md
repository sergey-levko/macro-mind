## Why

The MacroMind repository is empty — no backend, no frontend, no database tooling, no local dev environment. Before any feature can be built, a working project scaffold must exist that all subsequent vertical slices can build on top of.

## What Changes

- Initialize a Spring Boot 3 / Java 21 multi-module Maven project under `backend/`
- Initialize a React + TypeScript frontend project under `frontend/`
- Add Docker Compose configuration for local PostgreSQL 16
- Configure Liquibase with an initial empty changeset baseline
- Wire Spring AI dependency (Claude API) into the backend — no logic yet, just dependency + configuration placeholder
- Add a root `README.md` with local dev quick-start instructions

## Capabilities

### New Capabilities

- `project-scaffold`: Root repo layout, Docker Compose, shared tooling (`.gitignore`, root README)
- `backend-skeleton`: Spring Boot 3 app with Spring Web, Spring Data JPA, Spring AI, Liquibase, and PostgreSQL driver configured; health endpoint at `GET /api/v1/health`; application properties wired to Docker Compose DB
- `frontend-skeleton`: Vite + React + TypeScript project with TailwindCSS and Recharts installed; root `App.tsx` renders a placeholder page; proxy to backend configured

### Modified Capabilities

*(none — this is the initial scaffold)*

## Non-goals

- No business logic or domain features (meal logging, USDA search, AI advice, dashboards)
- No authentication or user management
- No production deployment configuration (CI/CD pipelines, cloud infra)
- No USDA FoodData Central API integration

## Impact

- **Database tables affected:** none created yet; Liquibase baseline changeset only establishes the changelog tracking table
- **API endpoints added:** `GET /api/v1/health` → `{ "status": "UP" }` (smoke-test endpoint)
- **Dependencies introduced:** Spring Boot 3, Spring AI, Spring Data JPA, Liquibase, PostgreSQL driver (backend); Vite, React, TypeScript, TailwindCSS, Recharts (frontend)
- All future vertical-slice changes depend on this scaffold existing
