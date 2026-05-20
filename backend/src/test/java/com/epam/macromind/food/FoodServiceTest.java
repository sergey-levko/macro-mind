package com.epam.macromind.food;

import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock FoodRepository foodRepository;
    @Mock UserRepository userRepository;
    @Mock UsdaFoodClient usdaFoodClient;

    @InjectMocks FoodService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID FOOD_ID = UUID.randomUUID();

    private static final CreateFoodRequest VALID_REQUEST = new CreateFoodRequest(
            "Chicken Breast", new BigDecimal("165"), new BigDecimal("31"),
            new BigDecimal("0"), new BigDecimal("3.6"));

    private Food sampleFood() {
        return new Food(USER_ID, "Chicken Breast", "CUSTOM",
                new BigDecimal("165"), new BigDecimal("31"),
                new BigDecimal("0"), new BigDecimal("3.6"));
    }

    @Test
    void createFood_success_returnsResponse() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(foodRepository.save(any())).thenReturn(sampleFood());

        FoodResponse response = service.createFood(USER_ID, VALID_REQUEST);

        assertThat(response.name()).isEqualTo("Chicken Breast");
        assertThat(response.source()).isEqualTo("CUSTOM");
    }

    @Test
    void createFood_userNotFound_throwsUserNotFoundException() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createFood(USER_ID, VALID_REQUEST))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getFoodById_exists_returnsResponse() {
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.of(sampleFood()));

        FoodResponse response = service.getFoodById(FOOD_ID);

        assertThat(response.name()).isEqualTo("Chicken Breast");
    }

    @Test
    void getFoodById_notFound_throwsFoodNotFoundException() {
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFoodById(FOOD_ID))
                .isInstanceOf(FoodNotFoundException.class)
                .hasMessageContaining(FOOD_ID.toString());
    }

    @Test
    void searchFoods_withTerm_returnsMatchingFoods() {
        when(foodRepository.findTop20ByUserIdAndNameContainingIgnoreCase(USER_ID, "chicken"))
                .thenReturn(List.of(sampleFood()));

        List<FoodResponse> results = service.searchFoods(USER_ID, "chicken");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Chicken Breast");
    }

    @Test
    void searchFoods_noTerm_returnsAllUserFoods() {
        when(foodRepository.findTop20ByUserId(USER_ID)).thenReturn(List.of(sampleFood()));

        List<FoodResponse> results = service.searchFoods(USER_ID, null);

        assertThat(results).hasSize(1);
    }

    @Test
    void deleteFood_success_deletesFood() {
        Food food = sampleFood();
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.of(food));

        service.deleteFood(USER_ID, FOOD_ID);

        verify(foodRepository).delete(food);
    }

    @Test
    void deleteFood_notFound_throwsFoodNotFoundException() {
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFood(USER_ID, FOOD_ID))
                .isInstanceOf(FoodNotFoundException.class);
    }

    @Test
    void deleteFood_differentUser_throwsFoodAccessDeniedException() {
        Food food = new Food(UUID.randomUUID(), "Other Food", "CUSTOM",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        when(foodRepository.findById(FOOD_ID)).thenReturn(Optional.of(food));

        assertThatThrownBy(() -> service.deleteFood(USER_ID, FOOD_ID))
                .isInstanceOf(FoodAccessDeniedException.class);
    }

    @Test
    void importFood_success_persistsWithUsdaSource() {
        UsdaFoodDto dto = new UsdaFoodDto("Brown Rice",
                List.of(
                        new UsdaFoodDto.FoodNutrient(new UsdaFoodDto.Nutrient(1008), new BigDecimal("370")),
                        new UsdaFoodDto.FoodNutrient(new UsdaFoodDto.Nutrient(1003), new BigDecimal("7.9")),
                        new UsdaFoodDto.FoodNutrient(new UsdaFoodDto.Nutrient(1005), new BigDecimal("77")),
                        new UsdaFoodDto.FoodNutrient(new UsdaFoodDto.Nutrient(1004), new BigDecimal("2.9"))
                ));
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(usdaFoodClient.fetch(12345)).thenReturn(dto);
        when(foodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FoodResponse response = service.importFood(USER_ID, new ImportFoodRequest(12345));

        assertThat(response.source()).isEqualTo("USDA");
        assertThat(response.name()).isEqualTo("Brown Rice");
    }

    @Test
    void importFood_usdaNotFound_throwsUsdaFoodNotFoundException() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(usdaFoodClient.fetch(99999)).thenThrow(new UsdaFoodNotFoundException(99999));

        assertThatThrownBy(() -> service.importFood(USER_ID, new ImportFoodRequest(99999)))
                .isInstanceOf(UsdaFoodNotFoundException.class);
    }

    @Test
    void importFood_usdaUnavailable_throwsUsdaServiceUnavailableException() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(usdaFoodClient.fetch(any(int.class)))
                .thenThrow(new UsdaServiceUnavailableException("unreachable", null));

        assertThatThrownBy(() -> service.importFood(USER_ID, new ImportFoodRequest(1)))
                .isInstanceOf(UsdaServiceUnavailableException.class);
    }
}
