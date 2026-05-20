package com.epam.macromind.goal;

import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
class NutritionalGoalService {

    private final NutritionalGoalRepository goalRepository;
    private final UserRepository userRepository;

    NutritionalGoalService(NutritionalGoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    NutritionalGoalResponse setGoal(UUID userId, SetNutritionalGoalRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        goalRepository.findByUserId(userId).ifPresent(goalRepository::delete);
        NutritionalGoal goal = goalRepository.save(new NutritionalGoal(
                userId, request.caloriesTarget(), request.proteinG(),
                request.carbsG(), request.fatG()));
        return toResponse(goal);
    }

    @Transactional(readOnly = true)
    NutritionalGoalResponse getGoal(UUID userId) {
        return goalRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new GoalNotFoundException(userId));
    }

    void deleteGoal(UUID userId) {
        NutritionalGoal goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new GoalNotFoundException(userId));
        goalRepository.delete(goal);
    }

    private NutritionalGoalResponse toResponse(NutritionalGoal goal) {
        return new NutritionalGoalResponse(
                goal.getId(), goal.getUserId(), goal.getCaloriesTarget(),
                goal.getProteinG(), goal.getCarbsG(), goal.getFatG());
    }
}
