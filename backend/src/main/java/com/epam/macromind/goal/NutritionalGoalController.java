package com.epam.macromind.goal;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nutritional-goals")
class NutritionalGoalController {

    private final NutritionalGoalService service;

    NutritionalGoalController(NutritionalGoalService service) {
        this.service = service;
    }

    @PutMapping
    ResponseEntity<NutritionalGoalResponse> setGoal(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SetNutritionalGoalRequest request) {
        return ResponseEntity.ok(service.setGoal(userId, request));
    }

    @GetMapping
    ResponseEntity<NutritionalGoalResponse> getGoal(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(service.getGoal(userId));
    }

    @DeleteMapping
    ResponseEntity<Void> deleteGoal(
            @RequestHeader("X-User-Id") UUID userId) {
        service.deleteGoal(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    ResponseEntity<GoalSuggestionResponse> generateGoal(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(service.generateGoal(userId));
    }
}
