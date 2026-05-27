package com.epam.macromind.template;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.meal.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
class MealTemplateService {

    private final MealTemplateRepository templateRepository;
    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;

    MealTemplateService(MealTemplateRepository templateRepository,
                        MealLogRepository mealLogRepository,
                        FoodRepository foodRepository) {
        this.templateRepository = templateRepository;
        this.mealLogRepository = mealLogRepository;
        this.foodRepository = foodRepository;
    }

    MealTemplateResponse saveTemplate(UUID userId, SaveTemplateRequest request) {
        Instant start = request.date().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = request.date().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<MealLog> logs = mealLogRepository
                .findWithItemsByUserIdAndLoggedAtBetween(userId, start, end);
        if (logs.isEmpty()) {
            throw new NoMealsForDateException(request.date().toString());
        }

        MealTemplate template = new MealTemplate(userId, request.name().trim());
        logs.forEach(log ->
                log.getItems().forEach(item ->
                        template.getItems().add(
                                new MealTemplateItem(template, item.getFoodId(), log.getMealType(), item.getQuantityG())
                        )
                )
        );
        MealTemplate saved = templateRepository.save(template);

        Set<UUID> foodIds = saved.getItems().stream().map(MealTemplateItem::getFoodId).collect(Collectors.toSet());
        Map<UUID, Food> foodMap = foodRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));
        return toResponse(saved, foodMap);
    }

    @Transactional(readOnly = true)
    List<MealTemplateResponse> listTemplates(UUID userId) {
        List<MealTemplate> templates = templateRepository.findByUserId(userId);
        if (templates.isEmpty()) return List.of();

        Set<UUID> allFoodIds = templates.stream()
                .flatMap(t -> t.getItems().stream())
                .map(MealTemplateItem::getFoodId)
                .collect(Collectors.toSet());
        Map<UUID, Food> foodMap = foodRepository.findAllById(allFoodIds).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));

        return templates.stream().map(t -> toResponse(t, foodMap)).toList();
    }

    List<MealLogSummaryResponse> applyTemplate(UUID userId, UUID templateId, ApplyTemplateRequest request) {
        MealTemplate template = templateRepository.findById(templateId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new MealTemplateNotFoundException(templateId));

        Instant loggedAt = request.date().atStartOfDay(ZoneOffset.UTC).toInstant();

        Map<MealType, List<MealTemplateItem>> byType = template.getItems().stream()
                .collect(Collectors.groupingBy(MealTemplateItem::getMealType));

        Set<UUID> foodIds = template.getItems().stream()
                .map(MealTemplateItem::getFoodId).collect(Collectors.toSet());
        Map<UUID, Food> foodMap = foodRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));

        return byType.entrySet().stream().map(entry -> {
            MealLog log = new MealLog(userId, entry.getKey(), loggedAt);
            entry.getValue().forEach(item ->
                    log.getItems().add(new MealItem(log, item.getFoodId(), item.getQuantityG())));
            MealLog saved = mealLogRepository.save(log);
            MacroTotals totals = saved.getItems().stream()
                    .map(item -> computeMacros(item.getQuantityG(), foodMap.get(item.getFoodId())))
                    .reduce(MacroTotals.ZERO, MacroTotals::add);
            return new MealLogSummaryResponse(saved.getId(), saved.getMealType(), saved.getLoggedAt(), totals);
        }).toList();
    }

    void deleteTemplate(UUID userId, UUID templateId) {
        MealTemplate template = templateRepository.findById(templateId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new MealTemplateNotFoundException(templateId));
        templateRepository.delete(template);
    }

    private MealTemplateResponse toResponse(MealTemplate template, Map<UUID, Food> foodMap) {
        MacroTotals totals = template.getItems().stream()
                .map(item -> computeMacros(item.getQuantityG(), foodMap.get(item.getFoodId())))
                .reduce(MacroTotals.ZERO, MacroTotals::add);
        return new MealTemplateResponse(
                template.getId(),
                template.getName(),
                template.getCreatedAt(),
                template.getItems().size(),
                totals);
    }

    private MacroTotals computeMacros(BigDecimal quantityG, Food food) {
        if (food == null) return MacroTotals.ZERO;
        BigDecimal factor = quantityG.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        return new MacroTotals(
                scale(orZero(food.getCalories100g()).multiply(factor)),
                scale(orZero(food.getProteinG()).multiply(factor)),
                scale(orZero(food.getCarbsG()).multiply(factor)),
                scale(orZero(food.getFatG()).multiply(factor)));
    }

    private BigDecimal orZero(BigDecimal value) { return value != null ? value : BigDecimal.ZERO; }
    private BigDecimal scale(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
}
