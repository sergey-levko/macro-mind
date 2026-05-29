package com.epam.macromind.meal;

import com.epam.macromind.common.SecurityUtils;
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
    MealLogResponse create(@Valid @RequestBody CreateMealLogRequest request) {
        return service.createMealLog(SecurityUtils.currentUserId(), request);
    }

    @GetMapping("/{id}")
    MealLogResponse getById(@PathVariable UUID id) {
        return service.getMealLogById(id);
    }

    @GetMapping
    List<MealLogSummaryResponse> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getMealLogsByDate(SecurityUtils.currentUserId(), date);
    }

    @PatchMapping("/{id}")
    MealLogResponse update(@PathVariable UUID id,
                           @Valid @RequestBody UpdateMealLogRequest request) {
        return service.updateMealLog(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        service.deleteMealLog(SecurityUtils.currentUserId(), id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    MealItemResponse addItem(@PathVariable UUID id,
                             @Valid @RequestBody AddMealItemRequest request) {
        return service.addItem(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        service.removeItem(SecurityUtils.currentUserId(), id, itemId);
    }

}
