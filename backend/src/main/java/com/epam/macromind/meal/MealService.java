package com.epam.macromind.meal;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodNotFoundException;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
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
class MealService {

    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    MealService(MealLogRepository mealLogRepository, UserRepository userRepository,
                FoodRepository foodRepository) {
        this.mealLogRepository = mealLogRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    MealLogResponse createMealLog(UUID userId, CreateMealLogRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        Instant loggedAt = request.loggedAt() != null ? request.loggedAt() : Instant.now();
        MealLog log = mealLogRepository.save(new MealLog(userId, request.mealType(), loggedAt));
        return toResponse(log, Map.of());
    }

    @Transactional(readOnly = true)
    MealLogResponse getMealLogById(UUID logId) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new MealLogNotFoundException(logId));
        return toResponse(log, loadFoodMap(log.getItems()));
    }

    @Transactional(readOnly = true)
    List<MealLogSummaryResponse> getMealLogsByDate(UUID userId, LocalDate date) {
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<MealLog> logs = mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(userId, start, end);
        Set<UUID> allFoodIds = logs.stream()
                .flatMap(l -> l.getItems().stream())
                .map(MealItem::getFoodId)
                .collect(Collectors.toSet());
        Map<UUID, Food> foodMap = foodRepository.findAllById(allFoodIds)
                .stream().collect(Collectors.toMap(Food::getId, f -> f));
        return logs.stream().map(l -> toSummary(l, foodMap)).toList();
    }

    List<MealLogSummaryResponse> copyPreviousDay(UUID userId, LocalDate targetDate, MealType mealType) {
        LocalDate sourceDate = targetDate.minusDays(1);
        Instant sourceStart = sourceDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant sourceEnd = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        List<MealLog> sourceLogs = mealLogRepository
                .findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(userId, sourceStart, sourceEnd);
        if (mealType != null) {
            sourceLogs = sourceLogs.stream().filter(l -> l.getMealType() == mealType).toList();
        }
        if (sourceLogs.isEmpty()) return List.of();
        Instant targetLoggedAt = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Set<UUID> foodIds = sourceLogs.stream()
                .flatMap(l -> l.getItems().stream())
                .map(MealItem::getFoodId)
                .collect(Collectors.toSet());
        Map<UUID, Food> foodMap = foodRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));
        List<MealLog> newLogs = sourceLogs.stream().map(source -> {
            MealLog newLog = new MealLog(userId, source.getMealType(), targetLoggedAt);
            source.getItems().forEach(item ->
                    newLog.getItems().add(new MealItem(newLog, item.getFoodId(), item.getQuantityG())));
            return mealLogRepository.save(newLog);
        }).toList();
        return newLogs.stream().map(l -> toSummary(l, foodMap)).toList();
    }

    MealLogResponse updateMealLog(UUID userId, UUID logId, UpdateMealLogRequest request) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new MealLogNotFoundException(logId));
        if (!log.getUserId().equals(userId)) {
            throw new MealLogAccessDeniedException(logId);
        }
        log.setLoggedAt(request.loggedAt());
        mealLogRepository.save(log);
        return toResponse(log, loadFoodMap(log.getItems()));
    }

    void deleteMealLog(UUID userId, UUID logId) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new MealLogNotFoundException(logId));
        if (!log.getUserId().equals(userId)) {
            throw new MealLogAccessDeniedException(logId);
        }
        mealLogRepository.delete(log);
    }

    MealItemResponse addItem(UUID userId, UUID logId, AddMealItemRequest request) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new MealLogNotFoundException(logId));
        if (!log.getUserId().equals(userId)) {
            throw new MealLogAccessDeniedException(logId);
        }
        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() -> new FoodNotFoundException(request.foodId()));
        MealItem item = new MealItem(log, food.getId(), request.quantityG());
        log.getItems().add(item);
        mealLogRepository.save(log);
        return toItemResponse(item, food);
    }

    void removeItem(UUID userId, UUID logId, UUID itemId) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new MealLogNotFoundException(logId));
        if (!log.getUserId().equals(userId)) {
            throw new MealLogAccessDeniedException(logId);
        }
        MealItem item = log.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new MealItemNotFoundException(itemId));
        log.getItems().remove(item);
        mealLogRepository.save(log);
    }

    private Map<UUID, Food> loadFoodMap(List<MealItem> items) {
        Set<UUID> ids = items.stream().map(MealItem::getFoodId).collect(Collectors.toSet());
        return foodRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));
    }

    private MealLogResponse toResponse(MealLog log, Map<UUID, Food> foodMap) {
        List<MealItemResponse> itemResponses = log.getItems().stream()
                .map(item -> toItemResponse(item, foodMap.get(item.getFoodId())))
                .toList();
        MacroTotals totals = itemResponses.stream()
                .map(r -> new MacroTotals(r.calories(), r.proteinG(), r.carbsG(), r.fatG()))
                .reduce(MacroTotals.ZERO, MacroTotals::add);
        return new MealLogResponse(log.getId(), log.getUserId(), log.getMealType(),
                log.getLoggedAt(), itemResponses, totals);
    }

    private MealLogSummaryResponse toSummary(MealLog log, Map<UUID, Food> foodMap) {
        MacroTotals totals = log.getItems().stream()
                .map(item -> computeMacros(item.getQuantityG(), foodMap.get(item.getFoodId())))
                .reduce(MacroTotals.ZERO, MacroTotals::add);
        return new MealLogSummaryResponse(log.getId(), log.getMealType(), log.getLoggedAt(), totals);
    }

    private MealItemResponse toItemResponse(MealItem item, Food food) {
        BigDecimal qty = item.getQuantityG();
        String name = food != null ? food.getName() : "";
        MacroTotals macros = computeMacros(qty, food);
        return new MealItemResponse(item.getId(), item.getFoodId(), name, qty,
                macros.caloriesKcal(), macros.proteinG(), macros.carbsG(), macros.fatG());
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

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
