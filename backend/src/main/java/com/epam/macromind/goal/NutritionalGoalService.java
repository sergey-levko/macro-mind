package com.epam.macromind.goal;

import com.epam.macromind.user.User;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
class NutritionalGoalService {

    private final NutritionalGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;

    NutritionalGoalService(NutritionalGoalRepository goalRepository,
                           UserRepository userRepository,
                           @Qualifier("goalChatClient") ChatClient chatClient) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClient;
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

    @Transactional(readOnly = true)
    GoalSuggestionResponse generateGoal(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        String prompt = buildGoalPrompt(user);
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(GoalSuggestionResponse.class);
        } catch (Exception e) {
            throw new GoalGenerationException("Failed to generate goal suggestion from AI", e);
        }
    }

    private String buildGoalPrompt(User user) {
        return String.format("""
                You are a certified nutrition expert. Based on the following user profile, \
                suggest daily macro targets using the Mifflin-St Jeor TDEE formula \
                with an appropriate macro split for the stated goal.

                Goal type: %s
                Age: %s years
                Weight: %s kg
                Height: %s cm

                Return ONLY a JSON object with these four numeric fields (no explanation):
                { "caloriesTarget": <number>, "proteinG": <number>, "carbsG": <number>, "fatG": <number> }
                """,
                user.getGoalType(), user.getAge(), user.getWeightKg(), user.getHeightCm());
    }

    private NutritionalGoalResponse toResponse(NutritionalGoal goal) {
        return new NutritionalGoalResponse(
                goal.getId(), goal.getUserId(), goal.getCaloriesTarget(),
                goal.getProteinG(), goal.getCarbsG(), goal.getFatG());
    }
}
