package com.epam.macromind.coach;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoachConfig {

    @Bean
    ChatClient coachChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
