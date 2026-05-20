package com.epam.macromind.advice;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AiAdviceConfig {

    @Bean
    ChatClient aiAdviceChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
