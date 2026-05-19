# MacroMind

AI-powered nutritional tracking & personalized meal intelligence.

Log daily meals against the USDA FoodData Central database, visualize your macro progress, and receive personalized nutrition advice powered by Claude AI.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose

## Quick Start

### 1. Start the database

```bash
docker compose up -d
```

PostgreSQL 16 starts on `localhost:5432` (database: `macromind`, user: `macromind`, password: `macromind`).

### 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

> **Spring AI:** Set the `ANTHROPIC_API_KEY` environment variable to enable AI features.
> ```bash
> export ANTHROPIC_API_KEY=sk-ant-...
> ```

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app opens at `http://localhost:5173`. API calls are proxied to the backend automatically.

### 4. Verify

```bash
curl http://localhost:8080/api/v1/health
# {"status":"UP"}
```

## Project Structure

```
macro-mind/
├── backend/          # Spring Boot 3 / Java 21
├── frontend/         # React + TypeScript (Vite)
├── docker-compose.yml
└── README.md
```

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring AI, Spring Data JPA |
| Database | PostgreSQL 16, Liquibase |
| Frontend | React, TypeScript, Vite, TailwindCSS, Recharts |
| AI | Claude API (via Spring AI) |
