## 1. Git Branch

- [x] 1.1 Create and switch to branch `feat/user-management`

## 2. Liquibase — Users Table

- [x] 2.1 Create `backend/src/main/resources/db/changelog/changes/0002-users.yaml`: add `goal_type_enum` PostgreSQL type and `users` table (id UUID PK, email UNIQUE NOT NULL, name NOT NULL, age, weight_kg, height_cm, goal_type using `goal_type_enum`); commit `chore: add Liquibase changeset for users table`

## 3. User Domain Slice

- [x] 3.1 Create `GoalType` enum (`LOSE_WEIGHT`, `MAINTAIN_WEIGHT`, `GAIN_MUSCLE`) and `User` JPA entity mapped to the `users` table; commit `feat: add User entity and GoalType enum`
- [x] 3.2 Create `UserRepository` (extends `JpaRepository<User, UUID>`); commit `feat: add UserRepository`
- [x] 3.3 Create `CreateUserRequest` (Bean Validation: `@NotBlank` on name/email, `@NotNull` on others) and `UserResponse` DTOs; commit `feat: add user request/response DTOs`
- [x] 3.4 Implement `UserService` with `createUser(CreateUserRequest)` (catches `DataIntegrityViolationException` → `409`) and `getUserById(UUID)` (throws `UserNotFoundException` → `404`); commit `feat: add UserService`
- [x] 3.5 Implement `UserController` with `POST /api/v1/users` (201) and `GET /api/v1/users/{id}` (200); add `GlobalExceptionHandler` entries for `UserNotFoundException` (404), `EmailAlreadyExistsException` (409), and `MethodArgumentNotValidException` (400); commit `feat: add UserController and exception handling`

## 4. Unit Tests

- [x] 4.1 Write `UserServiceTest` using Mockito: register success, duplicate email → exception, user not found → exception; commit `test: add UserService unit tests`
- [x] 4.2 Write `UserControllerTest` using `@WebMvcTest`: 201 on create, 409 on duplicate email, 400 on missing fields, 200 on get, 404 on unknown id, 400 on invalid UUID; commit `test: add UserController unit tests`

## 5. Integration Tests

- [x] 5.1 Write `UserIntegrationTest` using Testcontainers + `@SpringBootTest`: full round-trip register and retrieve over real PostgreSQL, duplicate email returns 409, unknown id returns 404; commit `test: add user management integration tests`

## 6. Pull Request

- [x] 6.1 Push branch `feat/user-management` to remote and open a pull request targeting `main` with title `feat: user management (register + profile endpoints)`
