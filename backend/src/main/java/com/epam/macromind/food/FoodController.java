package com.epam.macromind.food;

import com.epam.macromind.common.SecurityUtils;
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
    FoodResponse create(@Valid @RequestBody CreateFoodRequest request) {
        return service.createFood(SecurityUtils.currentUserId(), request);
    }

    @GetMapping("/{id}")
    FoodResponse getById(@PathVariable UUID id) {
        return service.getFoodById(id);
    }

    @GetMapping
    List<FoodResponse> search(@RequestParam(required = false) String search) {
        return service.searchFoods(SecurityUtils.currentUserId(), search);
    }

    @PutMapping("/{id}")
    FoodResponse update(@PathVariable UUID id,
                        @Valid @RequestBody UpdateFoodRequest request) {
        return service.updateFood(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        service.deleteFood(SecurityUtils.currentUserId(), id);
    }

    @GetMapping("/usda-search")
    List<UsdaFoodResult> searchUsda(@RequestParam String q) {
        try {
            return service.searchUsda(SecurityUtils.currentUserId(), q);
        } catch (UsdaServiceUnavailableException e) {
            return emptyList();
        }
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    FoodResponse importFood(@Valid @RequestBody ImportFoodRequest request) {
        return service.importFood(SecurityUtils.currentUserId(), request);
    }
}
