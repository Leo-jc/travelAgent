package com.serain.travelagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.rag
 * @Author: Serain
 * @CreateTime: 2026-05-16  21:10
 * @Description: 阿里云百炼云知识库配置类，用于配置和初始化云知识库检索增强顾问
 * @Version: 1.0
 */
@Configuration
@Slf4j
public class TravelAgentCloudConfig {

    /**
     * 云知识库索引名称（需要在阿里云百炼平台提前创建）
     */
    private static final String KNOWLEDGE_INDEX_NAME = "旅行助手";
    

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 创建 DashScope API 实例
     * @return DashScopeApi 实例
     */
    @Bean
    public DashScopeApi dashScopeApi() {
        log.info("初始化 DashScope API，使用云知识库: {}", KNOWLEDGE_INDEX_NAME);
        return new DashScopeApi(apiKey);
    }

    /**
     * 创建云知识库检索增强顾问（RetrievalAugmentationAdvisor）
     * 该 Advisor 会从阿里云百炼云知识库中检索相关文档，并增强 AI 的回答
     * 
     * @param dashScopeApi DashScope API 实例
     * @return RetrievalAugmentationAdvisor 实例，可作为 Advisor 使用
     */
    @Bean
    public Advisor cloudRagAdvisor(DashScopeApi dashScopeApi) {
        log.info("创建云知识库检索增强顾问，知识库名称: {}", KNOWLEDGE_INDEX_NAME);
        
        // 配置知识库检索选项
        DashScopeDocumentRetrieverOptions retrieverOptions = DashScopeDocumentRetrieverOptions.builder()
                .withIndexName(KNOWLEDGE_INDEX_NAME)
                .build();
        
        // 创建文档检索器
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi, retrieverOptions);
        
        // 构建 RetrievalAugmentationAdvisor（模块化 RAG 组件）
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
        
        log.info("✅ 云知识库检索增强顾问（RetrievalAugmentationAdvisor）创建成功");
        return advisor;
    }
}
