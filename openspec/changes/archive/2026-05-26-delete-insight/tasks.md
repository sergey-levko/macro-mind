## 1. Backend — Delete Endpoint

- [x] 1.1 Add `deleteAdvice(UUID userId, UUID id)` to `AiAdviceService`: load the record with `findById`, throw `AdviceNotFoundException` if absent or if `advice.getUserId()` doesn't match `userId`; then call `adviceRepository.deleteById(id)`
- [x] 1.2 Add `DELETE /api/v1/advice/{id}` to `AiAdviceController` returning `ResponseEntity<Void>` 204; call `adviceService.deleteAdvice(SecurityUtils.currentUserId(), id)`
- [x] 1.3 Add unit tests to `AiAdviceServiceTest`: `deleteAdvice_success`, `deleteAdvice_notFound_throws404`, `deleteAdvice_wrongOwner_throws404`
- [x] 1.4 Add `DELETE /api/v1/advice/{id}` test to `AiAdviceControllerTest`: `deleteAdvice_success_returns204`, `deleteAdvice_notFound_returns404`
- [x] 1.5 Add integration test to `AiAdviceIntegrationTest`: generate an insight, then `DELETE /api/v1/advice/{id}` → 204, then `GET /api/v1/advice/{id}` → 404

## 2. Frontend — Delete Button in History Tab

- [x] 2.1 In `Coach.tsx`, add an `api.delete` call helper (using the shared `api` object) and a `deletingId` state (`string | null`) to track which insight is being confirmed/deleted
- [x] 2.2 In the History tab's per-item render, add a trash icon button that sets `deletingId` to the item's `id`; when `deletingId === item.id`, replace the icon with inline "Delete?" + Confirm + Cancel buttons
- [x] 2.3 On confirm, call `DELETE /api/v1/advice/{id}`, then remove the item from `history` state; reset `deletingId` to null on both success and cancel
