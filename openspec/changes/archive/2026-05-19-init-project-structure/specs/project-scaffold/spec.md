### Requirement: Root repository layout
The repository SHALL contain a `backend/` directory for the Spring Boot project, a `frontend/` directory for the React project, a `docker-compose.yml` at the root for local infrastructure, and a `README.md` with local dev quick-start instructions.

#### Scenario: Developer clones repo and sees expected structure
- **WHEN** a developer clones the repository
- **THEN** the root directory contains `backend/`, `frontend/`, `docker-compose.yml`, `README.md`, and `.gitignore`

### Requirement: Docker Compose provides local PostgreSQL
The `docker-compose.yml` SHALL define a `postgres` service running PostgreSQL 16, exposed on port `5432`, with a named volume for data persistence and environment variables for database name, user, and password.

#### Scenario: Developer starts local infrastructure
- **WHEN** `docker compose up -d` is run from the repo root
- **THEN** a PostgreSQL 16 container starts and is reachable at `localhost:5432` with the configured credentials

#### Scenario: Data persists across restarts
- **WHEN** the compose stack is stopped and restarted
- **THEN** previously written data is still present (named volume retained)

### Requirement: Root README quick-start
The `README.md` SHALL include step-by-step instructions to: start the database with Docker Compose, run the backend, run the frontend, and verify the health endpoint.

#### Scenario: Developer follows README to run the app
- **WHEN** a developer follows the README instructions in order
- **THEN** the backend health endpoint returns `200 OK` and the frontend loads in the browser without errors
