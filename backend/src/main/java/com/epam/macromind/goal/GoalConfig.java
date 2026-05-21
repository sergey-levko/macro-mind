package com.epam.macromind.goal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GoalConfig {

    @Bean
    ChatClient goalChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
