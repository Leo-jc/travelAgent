package com.serain.travelagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.app
 * @Author: Serain
 * @CreateTime: 2026-05-12  16:39
 * @Description: TODO
 * @Version: 1.0
 */
@SpringBootTest
class TravelAgentTest {

    @Resource
    private TravelAgent travelAgent;


    @Test
    void ask() {
        String chatId= UUID.randomUUID().toString();
        travelAgent.ask("给出中国热门旅游景点",chatId);
        travelAgent.ask("请给我推荐一个景点",chatId);
        travelAgent.ask("请给我推荐一个景点",chatId);
    }

    @Test
    void askWithRag() {
        String chatId = UUID.randomUUID().toString();
        
        // 测试1: 询问与文档相关的问题
        System.out.println("=== 测试1: RAG增强问答 - 欧洲旅游 ===");
        TravelAgent.travelAgentResponse response1 = travelAgent.askWithRag(
            "推荐一些美食",
            chatId
        );
        assertNotNull(response1);
        assertNotNull(response1.text());
        System.out.println("回答: " + response1.text());
        System.out.println("标签: " + response1.tags());
    }
}