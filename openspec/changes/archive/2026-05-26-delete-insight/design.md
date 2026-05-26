## Context

The History tab in `Coach.tsx` lists all saved insights but provides no way to remove them. The backend `ai_advice` table has no delete endpoint. This is a small, self-contained addition: one new REST endpoint and a delete button in the history list UI.

## Goals / Non-Goals

**Goals:**
- `DELETE /api/v1/advice/{id}` → 204 No Content (owned record) or 404 (not found / belongs to another user)
- Delete button per insight card in the History tab with inline confirmation before the actual API call
- After deletion, remove the card from local React state without a full list reload

**Non-Goals:**
- Deleting from the Daily or Weekly tabs
- Soft delete, undo, or restore
- Bulk delete

## Decisions

### 1. Ownership enforcement: 404 vs 403

**Decision:** Return 404 for both "record doesn't exist" and "record belongs to another user."

**Rationale:** Returning 403 would confirm to a caller that the resource exists but is owned by someone else — information disclosure. Treating both cases as 404 is the standard REST pattern for ownership-enforced resources.

**Alternative considered:** Load record → compare userId → throw 403 if mismatch. Rejected because it leaks existence.

### 2. Inline confirmation vs modal

**Decision:** Inline confirmation — clicking the trash icon flips the card into a "Confirm delete?" state with Confirm + Cancel buttons, replacing the icon.

**Rationale:** No new component dependency (no modal), works within the existing card layout, clearly scoped to the specific insight. A modal would require either a shared `Modal` component (doesn't exist yet) or an inline imperative approach.

**Alternative considered:** `window.confirm()`. Rejected — blocks the UI thread and looks inconsistent with the dark app theme.

### 3. Service layer: load-then-delete vs `deleteByIdAndUserId`

**Decision:** Load with `findById`, check ownership, then `deleteById`.

**Rationale:** Separating the ownership check from the delete makes the 404 reason explicit in service code (not found vs wrong owner both return `AdviceNotFoundException`). A `deleteByIdAndUserId` query would silently do nothing if the record exists but is owned by someone else, forcing a follow-up check anyway.

**Alternative considered:** `adviceRepository.deleteByIdAndUserId(id, userId)` returning affected rows. Rejected because Spring Data doesn't easily return affected-row counts in a void method, and the logic is harder to read.

## Risks / Trade-offs

- [Race condition] User deletes an insight that is still PENDING (async generation in progress) → the `AsyncAdviceGenerator` will try to load the record and throw. Mitigation: `AsyncAdviceGenerator.complete()` already wraps the update in a try/catch that calls `fail()`, but if the record is deleted before the async method runs, `findById` returns empty and `orElseThrow()` throws. The exception propagates and logs as an error but has no user impact since the record is already gone. Acceptable for now.

## Migration Plan

No schema changes. The `ai_advice` table already supports hard DELETE. Deploy backend then frontend — no coordinated rollout needed.
