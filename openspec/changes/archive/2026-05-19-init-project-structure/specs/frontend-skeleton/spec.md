### Requirement: React TypeScript project initialised
The frontend SHALL be a Vite + React + TypeScript project under `frontend/` with TailwindCSS and Recharts installed as dependencies.

#### Scenario: Frontend installs and builds without errors
- **WHEN** `npm install && npm run build` is executed from `frontend/`
- **THEN** the build completes with no errors and outputs files to `frontend/dist/`

#### Scenario: Frontend dev server starts
- **WHEN** `npm run dev` is executed from `frontend/`
- **THEN** the Vite dev server starts on port `5173` and the app is accessible in the browser

### Requirement: Placeholder home page
The frontend SHALL render a placeholder page with the text "MacroMind" and a subtitle when the root route `/` is loaded.

#### Scenario: Root route renders placeholder
- **WHEN** a browser navigates to `http://localhost:5173/`
- **THEN** the page displays the text "MacroMind" with no console errors

### Requirement: API proxy to backend
The Vite dev server SHALL proxy all requests to `/api/**` to `http://localhost:8080`.

#### Scenario: Frontend calls health endpoint via proxy
- **WHEN** the frontend dev server is running and a request is made to `/api/v1/health`
- **THEN** the request is forwarded to `http://localhost:8080/api/v1/health` and the response is returned to the caller

#### Scenario: Proxy does not affect non-API routes
- **WHEN** a request is made to a non-`/api` path (e.g., `/`)
- **THEN** the request is handled by the Vite dev server normally (not proxied)
