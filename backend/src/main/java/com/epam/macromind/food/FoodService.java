package com.epam.macromind.food;

import com.epam.macromind.common.PageResponse;
import com.epam.macromind.meal.MealItemRepository;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final UsdaFoodClient usdaFoodClient;
    private final MealItemRepository mealItemRepository;

    FoodService(FoodRepository foodRepository, UserRepository userRepository,
                UsdaFoodClient usdaFoodClient, MealItemRepository mealItemRepository) {
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.usdaFoodClient = usdaFoodClient;
        this.mealItemRepository = mealItemRepository;
    }

    FoodResponse createFood(UUID userId, CreateFoodRequest request) {
        validateUserExists(userId);
        Food food = new Food(userId, request.name(), "CUSTOM",
                request.calories100g(), request.proteinG(), request.carbsG(), request.fatG());
        return FoodResponse.from(foodRepository.save(food));
    }

    FoodResponse getFoodById(UUID id) {
        return foodRepository.findById(id)
                .map(FoodResponse::from)
                .orElseThrow(() -> new FoodNotFoundException(id));
    }

    PageResponse<FoodResponse> searchFoods(UUID userId, String search, int page, int size) {
        int cappedSize = Math.min(size, 50);
        var pageable = PageRequest.of(page, cappedSize);
        var foods = (search == null || search.isBlank())
                ? foodRepository.findByUserIdOrderByNameAsc(userId, pageable)
                : foodRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, search, pageable);
        return PageResponse.of(foods.map(FoodResponse::from));
    }

    FoodResponse updateFood(UUID userId, UUID foodId, UpdateFoodRequest request) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException(foodId));
        if (!food.getUserId().equals(userId)) {
            throw new FoodAccessDeniedException(foodId);
        }
        food.setName(request.name());
        food.setCalories100g(request.calories100g());
        food.setProteinG(request.proteinG());
        food.setCarbsG(request.carbsG());
        food.setFatG(request.fatG());
        return FoodResponse.from(foodRepository.save(food));
    }

    void deleteFood(UUID userId, UUID foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException(foodId));
        if (!food.getUserId().equals(userId)) {
            throw new FoodAccessDeniedException(foodId);
        }
        if (mealItemRepository.existsByFoodId(foodId)) {
            throw new FoodInUseException(foodId);
        }
        foodRepository.delete(food);
    }

    List<FoodResponse> getRecentFoods(UUID userId, int limit) {
        int capped = Math.min(limit, 10);
        return foodRepository.findRecentByUserId(userId, capped).stream()
                .map(FoodResponse::from)
                .toList();
    }

    List<UsdaFoodResult> searchUsda(UUID userId, String query) {
        validateUserExists(userId);
        return usdaFoodClient.search(query);
    }

    FoodResponse importFood(UUID userId, ImportFoodRequest request) {
        validateUserExists(userId);
        UsdaFoodDto dto = usdaFoodClient.fetch(request.fdcId());
        BigDecimal calories = dto.getNutrientAmount(1008);
        BigDecimal protein = dto.getNutrientAmount(1003);
        BigDecimal carbs = dto.getNutrientAmount(1005);
        BigDecimal fat = dto.getNutrientAmount(1004);
        Food food = new Food(userId, dto.description(), "USDA", calories, protein, carbs, fat);
        return FoodResponse.from(foodRepository.save(food));
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
