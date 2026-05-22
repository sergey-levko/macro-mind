package com.epam.macromind.advice;

import com.epam.macromind.goal.NutritionalGoalRepository;
import com.epam.macromind.user.UserRepository;
import com.epam.macromind.user.UserNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
class AiAdviceService {

    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final NutritionalGoalRepository goalRepository;
    private final AiAdviceRepository adviceRepository;
    private final AdvicePromptBuilder promptBuilder;

    AiAdviceService(@Qualifier("aiAdviceChatClient") ChatClient chatClient,
                    UserRepository userRepository,
                    NutritionalGoalRepository goalRepository,
                    AiAdviceRepository adviceRepository,
                    AdvicePromptBuilder promptBuilder) {
        this.chatClient = chatClient;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.adviceRepository = adviceRepository;
        this.promptBuilder = promptBuilder;
    }

    AiAdviceResponse generateAdvice(UUID userId, GenerateAdviceRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new NoGoalForAdviceException(userId));

        String systemPrompt = promptBuilder.buildSystemPrompt(user, goal);
        String userPrompt = promptBuilder.buildUserPrompt(userId, request.adviceType(), request.periodStart());

        String content = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (request.preview()) {
            return new AiAdviceResponse(null, userId, request.adviceType(), request.periodStart(), content, null);
        }

        var advice = adviceRepository.save(
                new AiAdvice(userId, request.adviceType(), content, request.periodStart()));
        return toResponse(advice);
    }

    @Transactional(readOnly = true)
    AiAdviceResponse getAdvice(UUID id) {
        return adviceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AdviceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    List<AiAdviceResponse> listAdvice(UUID userId, AdviceType adviceType, LocalDate periodStart) {
        List<AiAdvice> results;
        if (adviceType != null && periodStart != null) {
            results = adviceRepository.findByUserIdAndAdviceTypeAndPeriodStart(userId, adviceType, periodStart);
        } else if (adviceType != null) {
            results = adviceRepository.findByUserIdAndAdviceType(userId, adviceType);
        } else if (periodStart != null) {
            results = adviceRepository.findByUserIdAndPeriodStart(userId, periodStart);
        } else {
            results = adviceRepository.findByUserId(userId);
        }
        return results.stream().map(this::toResponse).toList();
    }

    private AiAdviceResponse toResponse(AiAdvice advice) {
        return new AiAdviceResponse(
                advice.getId(),
                advice.getUserId(),
                advice.getAdviceType(),
                advice.getPeriodStart(),
                advice.getContent(),
                advice.getCreatedAt()
        );
    }
}
