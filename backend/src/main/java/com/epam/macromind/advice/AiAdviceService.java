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
class AiAdviceService {

    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final NutritionalGoalRepository goalRepository;
    private final AiAdviceRepository adviceRepository;
    private final AdvicePromptBuilder promptBuilder;
    private final AsyncAdviceGenerator asyncAdviceGenerator;

    AiAdviceService(@Qualifier("aiAdviceChatClient") ChatClient chatClient,
                    UserRepository userRepository,
                    NutritionalGoalRepository goalRepository,
                    AiAdviceRepository adviceRepository,
                    AdvicePromptBuilder promptBuilder,
                    AsyncAdviceGenerator asyncAdviceGenerator) {
        this.chatClient = chatClient;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.adviceRepository = adviceRepository;
        this.promptBuilder = promptBuilder;
        this.asyncAdviceGenerator = asyncAdviceGenerator;
    }

    GenerateAdviceResult generateAdvice(UUID userId, GenerateAdviceRequest request) {
        if (!request.preview()) {
            var existing = adviceRepository
                    .findByUserIdAndAdviceTypeAndPeriodStart(userId, request.adviceType(), request.periodStart());
            if (!existing.isEmpty()) {
                return new GenerateAdviceResult(toResponse(existing.get(0)), false);
            }
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new NoGoalForAdviceException(userId));

        String systemPrompt = promptBuilder.buildSystemPrompt(user, goal);
        String userPrompt = promptBuilder.buildUserPrompt(userId, request.adviceType(), request.periodStart());

        if (request.preview()) {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            return new GenerateAdviceResult(
                    new AiAdviceResponse(null, userId, request.adviceType(), request.periodStart(),
                            content, AdviceStatus.COMPLETED, null),
                    false);
        }

        var advice = adviceRepository.save(new AiAdvice(userId, request.adviceType(), request.periodStart()));
        asyncAdviceGenerator.complete(advice.getId(), systemPrompt, userPrompt);
        return new GenerateAdviceResult(toResponse(advice), true);
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
                advice.getStatus(),
                advice.getCreatedAt()
        );
    }
}
