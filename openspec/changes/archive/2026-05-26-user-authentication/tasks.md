## 1. Database Schema

- [x] 1.1 Add Liquibase changeset: `password_hash VARCHAR(255) NOT NULL DEFAULT ''` column on `users` table + `UNIQUE` constraint on `email`

## 2. Backend — Auth Dependencies

- [x] 2.1 Add `spring-boot-starter-security` and `jjwt` (io.jsonwebtoken) dependencies to `pom.xml`

## 3. Backend — JWT Infrastructure

- [x] 3.1 Create `JwtService`: generate token (sub=userId, 7-day TTL, HS256), validate token, extract userId — signed with `JWT_SECRET` env var
- [x] 3.2 Create `JwtAuthFilter extends OncePerRequestFilter`: extract Bearer token, validate, set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
- [x] 3.3 Create `SecurityConfig`: permit `/api/v1/auth/**`, require auth on all other `/api/v1/**`; disable CSRF; register `JwtAuthFilter`
- [x] 3.4 Unit tests for `JwtService` (generate + validate + expired token)

## 4. Backend — Auth Slice

- [x] 4.1 Create `auth` package with `AuthController`, `AuthService`, `RegisterRequest`, `LoginRequest`, `AuthResponse` (`token`, `user`)
- [x] 4.2 Implement `POST /api/v1/auth/register`: validate input, check email uniqueness (409), hash password with BCrypt, create `users` row, return JWT + user profile (HTTP 201)
- [x] 4.3 Implement `POST /api/v1/auth/login`: look up user by email, verify BCrypt password (401 on mismatch or unknown email), return JWT + user profile (HTTP 200)
- [x] 4.4 Integration tests for register and login (success + error cases) using Testcontainers

## 5. Backend — Remove X-User-Id, Wire JWT userId

- [x] 5.1 Replace `@RequestHeader("X-User-Id") UUID userId` with `SecurityContextHolder` lookup in all controllers (`UserController`, `MealController`, `FoodController`, `DashboardController`, `NutritionalGoalController`, `AiAdviceController`, `CoachController`)
- [x] 5.2 Update all existing integration tests: register a user first, then authenticate with the returned token (add `Authorization: Bearer` header to requests)

## 6. Frontend — Auth Context and API Client

- [x] 6.1 Create `AuthContext` (React context): stores `token` and `user`, exposes `login(token, user)`, `logout()`, persists token to localStorage
- [x] 6.2 Update `api.ts`: read token from `AuthContext` (or localStorage), attach `Authorization: Bearer <token>` header; on 401 response call `logout()` and redirect to `/login`

## 7. Frontend — Login / Register Page

- [x] 7.1 Create `LoginPage`: email + password form, calls `POST /api/v1/auth/login`, stores result via `AuthContext.login()`, redirects to `/dashboard`; toggle to register mode
- [x] 7.2 Implement register mode on `LoginPage`: additional fields (name, age, weight, height, goal type), calls `POST /api/v1/auth/register`

## 8. Frontend — Route Protection and Logout

- [x] 8.1 Create `ProtectedRoute` component: renders children if token present, redirects to `/login` otherwise
- [x] 8.2 Wrap all existing routes in `ProtectedRoute`; add `/login` as a public route
- [x] 8.3 Add logout button to the app shell/nav; calls `AuthContext.logout()` and redirects to `/login`
