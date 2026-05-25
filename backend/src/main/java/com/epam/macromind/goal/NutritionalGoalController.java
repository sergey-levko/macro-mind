package com.epam.macromind.goal;

import com.epam.macromind.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutritional-goals")
class NutritionalGoalController {

    private final NutritionalGoalService service;

    NutritionalGoalController(NutritionalGoalService service) {
        this.service = service;
    }

    @PutMapping
    ResponseEntity<NutritionalGoalResponse> setGoal(@Valid @RequestBody SetNutritionalGoalRequest request) {
        return ResponseEntity.ok(service.setGoal(SecurityUtils.currentUserId(), request));
    }

    @GetMapping
    ResponseEntity<NutritionalGoalResponse> getGoal() {
        return ResponseEntity.ok(service.getGoal(SecurityUtils.currentUserId()));
    }

    @DeleteMapping
    ResponseEntity<Void> deleteGoal() {
        service.deleteGoal(SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    ResponseEntity<GoalSuggestionResponse> generateGoal() {
        return ResponseEntity.ok(service.generateGoal(SecurityUtils.currentUserId()));
    }
}
