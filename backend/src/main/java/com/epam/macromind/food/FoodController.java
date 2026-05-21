package com.epam.macromind.food;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping("/api/v1/foods")
class FoodController {

    private final FoodService service;

    FoodController(FoodService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    FoodResponse create(@RequestHeader("X-User-Id") UUID userId,
                        @Valid @RequestBody CreateFoodRequest request) {
        return service.createFood(userId, request);
    }

    @GetMapping("/{id}")
    FoodResponse getById(@PathVariable UUID id) {
        return service.getFoodById(id);
    }

    @GetMapping
    List<FoodResponse> search(@RequestHeader("X-User-Id") UUID userId,
                              @RequestParam(required = false) String search) {
        return service.searchFoods(userId, search);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@RequestHeader("X-User-Id") UUID userId,
                @PathVariable UUID id) {
        service.deleteFood(userId, id);
    }

    @GetMapping("/usda-search")
    List<UsdaFoodResult> searchUsda(@RequestHeader("X-User-Id") UUID userId,
                                    @RequestParam String q) {
        try {
            return service.searchUsda(userId, q);
        } catch (UsdaServiceUnavailableException e) {
            return emptyList();
        }
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    FoodResponse importFood(@RequestHeader("X-User-Id") UUID userId,
                            @Valid @RequestBody ImportFoodRequest request) {
        return service.importFood(userId, request);
    }
}
