## ADDED Requirements

### Requirement: Spring Boot application starts successfully
The backend SHALL be a valid Spring Boot 3 / Java 21 Maven project that starts without errors when PostgreSQL is available and the required environment variables are set.

#### Scenario: Application starts with Docker Compose database running
- **WHEN** PostgreSQL is running via Docker Compose and `./mvnw spring-boot:run` is executed from `backend/`
- **THEN** the application starts on port `8080` with no startup errors in the logs

#### Scenario: Application fails fast when database is unavailable
- **WHEN** PostgreSQL is not running and the application starts
- **THEN** the application exits with a clear connection error (not silently hang)

### Requirement: Health endpoint
The backend SHALL expose `GET /api/v1/health` returning HTTP `200` with body `{ "status": "UP" }`.

#### Scenario: Health check returns UP
- **WHEN** `GET /api/v1/health` is called while the application is running
- **THEN** the response is HTTP `200 OK` with `Content-Type: application/json` and body `{"status":"UP"}`

### Requirement: Liquibase baseline changeset
The backend SHALL include a Liquibase master changelog at `src/main/resources/db/changelog/db.changelog-master.yaml` with an initial baseline changeset that creates no tables but establishes the Liquibase tracking infrastructure.

#### Scenario: Liquibase runs on startup without errors
- **WHEN** the application starts against a fresh (empty) PostgreSQL database
- **THEN** Liquibase executes successfully and the `databasechangelog` tracking table exists

#### Scenario: Liquibase is idempotent on repeated starts
- **WHEN** the application is restarted against the same database
- **THEN** Liquibase detects no pending changesets and starts without errors

### Requirement: Spring AI dependency configured
The backend SHALL include `spring-ai-anthropic-spring-boot-starter` in `pom.xml` and read the Claude API key from the `ANTHROPIC_API_KEY` environment variable via `spring.ai.anthropic.api-key`.

#### Scenario: Application starts with API key set
- **WHEN** `ANTHROPIC_API_KEY` environment variable is set and the application starts
- **THEN** the Spring AI auto-configuration initialises without errors (no beans that call the API are created yet)

#### Scenario: Application starts without API key (dev mode)
- **WHEN** `ANTHROPIC_API_KEY` is not set and `spring.ai.anthropic.api-key` is not provided
- **THEN** the application logs a warning but still starts (AI features are not active in the skeleton)
