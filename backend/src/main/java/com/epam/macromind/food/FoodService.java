package com.epam.macromind.food;

import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final UsdaFoodClient usdaFoodClient;

    FoodService(FoodRepository foodRepository, UserRepository userRepository,
                UsdaFoodClient usdaFoodClient) {
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.usdaFoodClient = usdaFoodClient;
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

    List<FoodResponse> searchFoods(UUID userId, String search) {
        List<Food> foods = (search == null || search.isBlank())
                ? foodRepository.findTop20ByUserId(userId)
                : foodRepository.findTop20ByUserIdAndNameContainingIgnoreCase(userId, search);
        return foods.stream().map(FoodResponse::from).toList();
    }

    void deleteFood(UUID userId, UUID foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException(foodId));
        if (!food.getUserId().equals(userId)) {
            throw new FoodAccessDeniedException(foodId);
        }
        foodRepository.delete(food);
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
