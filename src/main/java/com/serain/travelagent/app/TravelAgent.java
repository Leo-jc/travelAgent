package com.serain.travelagent.app;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TravelAgent {

    @Resource
    private ChatClient chatClient;
    
    @Resource
    private ChatMemory chatMemory;

    @Resource
    private Advisor cloudRagAdvisor;

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

    /**
     * 基于RAG的旅行问答方法
     * 从向量存储中检索相关文档，增强AI回答的准确性
     * 
     * @param question 用户问题
     * @param conversationId 会话ID
     * @return 旅行助手响应
     */
    public travelAgentResponse askWithRag(String question, String conversationId) {
        travelAgentResponse chatResponse = chatClient.prompt()
                .system("你是一个旅行助手。请基于提供的上下文信息回答问题。如果上下文中没有相关信息，可以基于你的知识回答。" +
                        "请严格按照 JSON 格式返回结果，不要包含任何其他文本。\n" +
                        "JSON 格式：{\"text\": \"你的回答内容\", \"tags\": [\"标签1\", \"标签2\"]}")
                .user(question)
                .advisors(
                        cloudRagAdvisor,
                        // Memory Advisor: 保持对话历史
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(conversationId)
                                .build()
                )
                .call()
                .entity(travelAgentResponse.class);
        log.info("RAG response: {}", chatResponse);
        return chatResponse;
    }

}
