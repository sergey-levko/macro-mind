## Why

Users have no way to remove unwanted or stale insights from their history. Insights accumulate over time and there is no way to clean up entries that are outdated, incorrect, or simply no longer relevant.

## What Changes

- Add `DELETE /api/v1/advice/{id}` endpoint that removes a saved insight record (owned by the requesting user)
- Add a delete button to each insight card in the History tab of `Coach.tsx`; clicking it shows an inline confirmation then calls the API and removes the card from the list
- Ownership check: users may only delete their own insights (404 if not found, 403 if owned by another user)

## Non-goals

- Deleting insights from the Daily or Weekly tabs (only History tab gets the delete action)
- Bulk delete or select-all
- Soft delete / archive (hard delete only)
- Undo/restore after deletion

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- `ai-advice`: new `DELETE /api/v1/advice/{id}` requirement added

## Impact

- **Backend**: `AiAdviceController` + `AiAdviceService` + `AiAdviceRepository` (Spring Data `deleteById` — no new query needed)
- **Frontend**: `Coach.tsx` — History tab item row gains a delete button and confirmation state
- **API**: new endpoint `DELETE /api/v1/advice/{id}` → 204 No Content on success, 404 if not found
- **Database**: `ai_advice` table (hard DELETE, no schema change needed)
