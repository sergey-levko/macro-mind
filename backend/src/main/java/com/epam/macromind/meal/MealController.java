package com.epam.macromind.meal;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meal-logs")
class MealController {

    private final MealService service;

    MealController(MealService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MealLogResponse create(@RequestHeader("X-User-Id") UUID userId,
                           @Valid @RequestBody CreateMealLogRequest request) {
        return service.createMealLog(userId, request);
    }

    @GetMapping("/{id}")
    MealLogResponse getById(@PathVariable UUID id) {
        return service.getMealLogById(id);
    }

    @GetMapping
    List<MealLogSummaryResponse> listByDate(@RequestHeader("X-User-Id") UUID userId,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getMealLogsByDate(userId, date);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        service.deleteMealLog(userId, id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    MealItemResponse addItem(@RequestHeader("X-User-Id") UUID userId,
                             @PathVariable UUID id,
                             @Valid @RequestBody AddMealItemRequest request) {
        return service.addItem(userId, id, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeItem(@RequestHeader("X-User-Id") UUID userId,
                    @PathVariable UUID id,
                    @PathVariable UUID itemId) {
        service.removeItem(userId, id, itemId);
    }
}
