package com.serain.travelagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.rag
 * @Author: Serain
 * @CreateTime: 2026-05-16  20:05
 * @Description: 旅行代理向量存储配置类，用于配置和初始化向量存储
 * @Version: 1.0
 */
@Slf4j
public class TravelAgentVectorStoreConfig {

    private TravelDocumentLoader travelDocumentLoader;

    @Resource
    private EmbeddingModel embeddingModel;

    /**
     * 创建并配置向量存储Bean
     * @return VectorStore实例
     */
    @Bean
    public VectorStore vectorStore() {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 应用启动完成后初始化向量存储
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> vectorStoreInitializer(VectorStore vectorStore) {
        return event -> {
            try {
                log.info("开始初始化向量存储...");

                // 加载所有旅行文档（已在 TravelDocumentLoader 中配置为更大的分割块）
                List<Document> documents = travelDocumentLoader.loadAllDocuments("document");

                if (documents != null && !documents.isEmpty()) {
                    log.info("加载了 {} 个文档片段，开始向量化...", documents.size());

                    // 添加文档到向量存储
                    vectorStore.add(documents);

                    log.info("✅ 成功向量化并存储 {} 个文档片段", documents.size());
                } else {
                    log.warn("⚠️ 未找到任何文档进行向量化处理");
                }

            } catch (Exception e) {
                log.error("❌ 初始化向量存储失败", e);
                throw new RuntimeException("向量存储初始化失败: " + e.getMessage(), e);
            }
        };
    }
}
