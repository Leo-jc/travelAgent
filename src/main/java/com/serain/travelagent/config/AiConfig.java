package com.serain.travelagent.config;

import com.serain.travelagent.app.advisor.ReReadingAdvisor;
import com.serain.travelagent.app.advisor.SimpleLoggerAdvisor;
import com.serain.travelagent.chatmemory.FileBasedChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        String fileDir= System.getProperty("user.dir") + "/tmp/chatMemory";
        return new FileBasedChatMemory(fileDir);
    }

    @Bean
    public ChatClient chatClient(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        new ReReadingAdvisor()
                )
                .build();
    }
}
