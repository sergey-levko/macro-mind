## 1. Database — Liquibase changeset

- [x] 1.1 Add Liquibase changeset creating `meal_templates` (id UUID PK, user_id UUID FK → users, name VARCHAR(100) NOT NULL, created_at TIMESTAMP) and `meal_template_items` (id UUID PK, template_id UUID FK → meal_templates ON DELETE CASCADE, food_id UUID FK → foods, meal_type meal_type_enum NOT NULL, quantity_g DECIMAL NOT NULL)

## 2. Backend — Entities and repositories

- [x] 2.1 Create `MealTemplate` JPA entity and `MealTemplateItem` JPA entity (with `@ManyToOne` to `MealTemplate` and `@OneToMany(cascade=ALL, orphanRemoval=true)` back-reference) in `com.epam.macromind.template`
- [x] 2.2 Create `MealTemplateRepository` (extends `JpaRepository`, add `findByUserId`) and `MealTemplateItemRepository` in the same slice

## 3. Backend — Service and DTOs

- [x] 3.1 Create request/response DTOs: `SaveTemplateRequest(name, date)`, `ApplyTemplateRequest(date)`, `MealTemplateResponse(id, name, createdAt, itemCount, MacroTotals totals)`, `MealTemplateItemResponse(foodName, mealType, quantityG)`
- [x] 3.2 Implement `MealTemplateService.saveTemplate(userId, SaveTemplateRequest)`: load all meal items for the given date, throw `400` if none found, create `MealTemplate` + `MealTemplateItem` rows, return `MealTemplateResponse`
- [x] 3.3 Implement `MealTemplateService.listTemplates(userId)`: load all templates for user, compute macro totals from current food data, return list of `MealTemplateResponse`
- [x] 3.4 Implement `MealTemplateService.applyTemplate(userId, templateId, ApplyTemplateRequest)`: load template (404 if not owned), group items by meal_type, create one `MealLog` per type with items, return list of `MealLogSummary`
- [x] 3.5 Implement `MealTemplateService.deleteTemplate(userId, templateId)`: load template (404 if not owned), delete it (cascade removes items)

## 4. Backend — Controller and integration tests

- [x] 4.1 Create `MealTemplateController` with routes: `POST /api/v1/meal-templates` → `saveTemplate` (201), `GET /api/v1/meal-templates` → `listTemplates` (200), `POST /api/v1/meal-templates/{id}/apply` → `applyTemplate` (200), `DELETE /api/v1/meal-templates/{id}` → `deleteTemplate` (204)
- [x] 4.2 Write `MealTemplateIntegrationTest` covering: save with meals returns 201, save with no meals returns 400, list returns templates with macro totals, apply creates meal logs, apply returns 404 for wrong user, delete returns 204 then GET returns 404

## 5. Frontend — Types and API

- [x] 5.1 Add `MealTemplate` type to `frontend/src/lib/types.ts`: `{ id: string; name: string; createdAt: string; itemCount: number; totals: MacroTotals }`

## 6. Frontend — Templates tab in MealLog page

- [x] 6.1 Add tab strip (Log / Templates) to the Meal Log page header; add `activeTab` state (`'log' | 'templates'`); show existing meal sections when `activeTab === 'log'` and a new `TemplatesTab` component when `activeTab === 'templates'`
- [x] 6.2 Implement `TemplatesTab` component in `MealLog.tsx`: fetches `GET /api/v1/meal-templates` on mount, renders a list of template cards (name, item count, calories/protein/carbs/fat totals), empty state message when list is empty
- [x] 6.3 Add "Save today as template" button to `TemplatesTab`: disabled when `selectedDate` has no meal logs (derive from parent `logs` state); on click prompt for name via `window.prompt`, POST to `POST /api/v1/meal-templates` with `{ name, date: selectedDate }`, refresh template list on success
- [x] 6.4 Add "Apply" button to each template card: calls `POST /api/v1/meal-templates/{id}/apply` with `{ date: selectedDate }`, on success switches `activeTab` to `'log'` and calls `loadLogs()` to refresh the meal log view
- [x] 6.5 Add "Delete" button to each template card: calls `DELETE /api/v1/meal-templates/{id}` after `window.confirm`, removes the template from the list on success
