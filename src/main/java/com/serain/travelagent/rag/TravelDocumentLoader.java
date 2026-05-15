package com.serain.travelagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.rag
 * @Author: Serain
 * @CreateTime: 2026-05-15  20:47
 * @Description: 旅行文档加载器，用于加载所有MD格式的文档
 * @Version: 1.0
 */

@Slf4j
public class TravelDocumentLoader {

    /**
     * 加载指定目录下的所有Markdown文档
     * @param directoryPath 文档目录路径
     * @return 文档列表
     */
    public List<Document> loadAllDocuments(String directoryPath) {
        List<Document> documents = new ArrayList<>();
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // 确保路径格式正确，移除可能的前导斜杠
            String cleanPath = directoryPath.startsWith("/") ? directoryPath.substring(1) : directoryPath;
            String pattern = "classpath:" + cleanPath + "/*.md";
            
            log.info("尝试加载文档，路径模式: {}", pattern);
            
            // 匹配目录下所有的.md文件
            Resource[] resources = resolver.getResources(pattern);
            
            if (resources.length == 0) {
                log.warn("未找到任何MD文件，请检查路径: {}", pattern);
                return documents;
            }
            
            log.info("找到 {} 个MD文件", resources.length);
            
            for (Resource resource : resources) {
                if (resource.exists()) {
                    try {
                        log.info("正在处理文件: {}", resource.getFilename());
                        
                        // 创建配置对象，设置更大的文档分割
                        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                                .withHorizontalRuleCreateDocument(false) // 不按水平线分割，保持文档完整性
                                .withIncludeCodeBlock(true)              // 包含代码块
                                .withIncludeBlockquote(true)             // 包含引用块
                                .build();
                        
                        // 使用 Resource 和 Config 构造 MarkdownDocumentReader
                        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                        List<Document> docs = reader.get();
                        
                        // 为每个文档添加文件名元信息
                        String filename = resource.getFilename();
                        for (Document doc : docs) {
                            doc.getMetadata().put("filename", filename);
                        }
                        documents.addAll(docs);
                        log.info("加载文档: {}, 片段数: {}", filename, docs.size());
                    } catch (Exception e) {
                        log.error("处理文件失败: {}, 错误: {}", resource.getFilename(), e.getMessage());
                        log.debug("详细错误信息", e);
                        // 继续处理下一个文件，不中断整个流程
                    }
                }
            }
            
            log.info("成功加载了 {} 个文档片段", documents.size());
            
        } catch (Exception e) {
            log.error("加载文档异常", e);
            throw new RuntimeException("加载文档失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
}
