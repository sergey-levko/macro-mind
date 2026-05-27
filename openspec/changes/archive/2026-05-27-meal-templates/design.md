## Context

The existing `copy-previous-day` feature copies all (or one meal type's) logs from yesterday to today. It is one-shot — it pulls the previous day's actual logged meals each time. Users who eat the same thing every day need a persistent, named reference they can apply on any date without worrying about what they happened to log yesterday.

The new template system stores food + quantity + meal type without a date. Applying a template on a given date creates new `meal_logs` and `meal_items` rows for that date, exactly like manually logging each meal.

## Goals / Non-Goals

**Goals:**
- Save the currently logged meals for a date as a named template
- List all saved templates for the user
- Apply a template to a date: create meal logs and items for each template item grouped by meal type
- Delete a template
- Surface the feature in a Templates tab on the Meal Log page

**Non-Goals:**
- Template editing (delete and recreate)
- Partial apply or meal-type filtering on apply
- Template sharing between users
- Auto-apply / scheduling
- AI integration with templates

## Decisions

**Separate `meal_templates` + `meal_template_items` tables rather than reusing `meal_logs`**
Templates have no date and are never shown in the daily timeline. Reusing `meal_logs` with a sentinel date would pollute queries and confuse the data model. Two new tables keep the domain clean.

**Save-from-date approach for template creation**
Rather than a form where the user manually adds items, the save endpoint accepts a `date` parameter and copies all meal items logged on that date into the template. This is the fastest path for the habitual-eater use case and avoids building a separate item-editing UI.

**Apply creates new `meal_logs` grouped by meal type**
Each distinct `meal_type` in the template gets one new `meal_log` row, with all items for that type added as `meal_items`. This mirrors how copy-previous-day works and keeps the rest of the app (daily summaries, charts) unchanged.

**Backend vertical slice: `com.epam.macromind.template`**
Follows the existing slice pattern: `MealTemplate` entity, `MealTemplateItem` entity, `MealTemplateRepository`, `MealTemplateItemRepository`, `MealTemplateService`, `MealTemplateController`, request/response DTOs.

**Liquibase changeset for schema**
Two new tables added via a Liquibase changeset in `db/changelog/`:
- `meal_templates`: `id UUID PK`, `user_id UUID FK → users`, `name VARCHAR(100) NOT NULL`, `created_at TIMESTAMP`
- `meal_template_items`: `id UUID PK`, `template_id UUID FK → meal_templates CASCADE DELETE`, `food_id UUID FK → foods`, `meal_type meal_type_enum NOT NULL`, `quantity_g DECIMAL NOT NULL`

**Frontend: Templates tab in MealLog page**
A new tab strip (Log / Templates) is added at the top of the Meal Log page. The Templates view shows a list of saved templates with name, item count, and macro totals preview, plus a "Save today as template" button at the top that prompts for a name. Each template card has an Apply and a Delete button.

## Risks / Trade-offs

- **Food deletion breaks template items** — if a food is deleted, the template item's `food_id` FK becomes invalid. Mitigation: cascade or soft-delete is out of scope; template items referencing deleted foods will fail gracefully on apply (skip or error). For now, rely on foods rarely being deleted.
- **Apply creates duplicate logs** — applying the same template twice on the same date creates duplicate meal logs. Mitigation: accepted; same behaviour as copy-previous-day. No dedup guard needed.
- **Template totals are stale** — template items store quantity but not pre-computed macros; the apply endpoint recalculates from current food data. If food macros change, the applied totals may differ from when the template was saved. This is desirable (always use current food data).
