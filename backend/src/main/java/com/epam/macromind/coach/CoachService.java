package com.epam.macromind.coach;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.meal.MealItem;
import com.epam.macromind.meal.MealLog;
import com.epam.macromind.meal.MealLogRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class CoachService {

    private final ChatClient chatClient;
    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;

    CoachService(@Qualifier("coachChatClient") ChatClient chatClient,
                 MealLogRepository mealLogRepository,
                 FoodRepository foodRepository) {
        this.chatClient = chatClient;
        this.mealLogRepository = mealLogRepository;
        this.foodRepository = foodRepository;
    }

    ChatResponse chat(UUID userId, String message) {
        String context = buildMealContext(userId);
        String systemPrompt = """
                You are a personal nutrition coach. Answer the user's nutrition questions helpfully and concisely.
                Use the meal log summary below to give personalized, context-aware advice.

                %s
                """.formatted(context);

        String reply = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();

        return new ChatResponse(reply);
    }

    private String buildMealContext(UUID userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant start = today.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<MealLog> logs = mealLogRepository
                .findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(userId, start, end);

        if (logs.isEmpty()) {
            return "Meal log (last 7 days): No meals logged.";
        }

        StringBuilder sb = new StringBuilder("Meal log (last 7 days):\n");
        for (MealLog log : logs) {
            LocalDate day = log.getLoggedAt().atZone(ZoneOffset.UTC).toLocalDate();
            sb.append(String.format("  [%s %s]%n", day, log.getMealType()));
            for (MealItem item : log.getItems()) {
                Food food = foodRepository.findById(item.getFoodId()).orElse(null);
                if (food == null) continue;
                BigDecimal factor = item.getQuantityG().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal kcal = multiply(food.getCalories100g(), factor);
                BigDecimal protein = multiply(food.getProteinG(), factor);
                BigDecimal carbs = multiply(food.getCarbsG(), factor);
                BigDecimal fat = multiply(food.getFatG(), factor);
                sb.append(String.format("    - %s (%.0fg): %.0f kcal | P %.1fg | C %.1fg | F %.1fg%n",
                        food.getName(), item.getQuantityG(), kcal, protein, carbs, fat));
            }
        }
        return sb.toString();
    }

    private BigDecimal multiply(BigDecimal value, BigDecimal factor) {
        if (value == null) return BigDecimal.ZERO;
        return value.multiply(factor).setScale(1, RoundingMode.HALF_UP);
    }
}
