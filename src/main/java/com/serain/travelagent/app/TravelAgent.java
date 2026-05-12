package com.serain.travelagent.app;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TravelAgent {

    @Resource
    private ChatClient chatClient;
    
    @Resource
    private ChatMemory chatMemory;

    @JsonClassDescription("旅行助手的响应")
    record travelAgentResponse(
            @JsonPropertyDescription("回答的内容") String text,
            @JsonPropertyDescription("相关的标签列表") List<String> tags
    ) {
    }

    public travelAgentResponse ask(String question,String conversationId) {
        travelAgentResponse chatResponse = chatClient.prompt()
                .system("你是一个旅行助手。请严格按照 JSON 格式返回结果，不要包含任何其他文本。\n" +
                        "JSON 格式：{\"text\": \"你的回答内容\", \"tags\": [\"标签1\", \"标签2\"]}")
                .user(question)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build())
                .call()
                .entity(travelAgentResponse.class);
        log.info("report +{}", chatResponse);
        return chatResponse;
    }

}
