package com.epam.macromind.advice;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
class AsyncAdviceGenerator {

    private final ChatClient chatClient;
    private final AiAdviceRepository adviceRepository;

    AsyncAdviceGenerator(@Qualifier("aiAdviceChatClient") ChatClient chatClient,
                         AiAdviceRepository adviceRepository) {
        this.chatClient = chatClient;
        this.adviceRepository = adviceRepository;
    }

    @Async
    @Transactional
    public void complete(UUID adviceId, String systemPrompt, String userPrompt) {
        var advice = adviceRepository.findById(adviceId).orElseThrow();
        try {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            advice.complete(content);
        } catch (Exception e) {
            advice.fail();
        }
        adviceRepository.save(advice);
    }
}
