package com.serain.travelagent.app.advisor;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Re-Reading Advisor - 通过重新阅读问题提升AI回答准确性
 * 在发送请求前，将用户问题重复一次，让AI模型再次确认问题内容
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    private final String re2AdviseTemplate;
    private int order = 0;

    public ReReadingAdvisor() {
        this(DEFAULT_RE2_ADVISE_TEMPLATE);
    }

    public ReReadingAdvisor(String re2AdviseTemplate) {
        this.re2AdviseTemplate = re2AdviseTemplate;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 前置处理：增强用户提示词
        AdvisedRequest enhancedRequest = enhanceRequest(advisedRequest);
        
        // 继续执行调用链
        return chain.nextAroundCall(enhancedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 前置处理：增强用户提示词
        AdvisedRequest enhancedRequest = enhanceRequest(advisedRequest);
        
        // 继续执行流式调用链
        return chain.nextAroundStream(enhancedRequest);
    }

    /**
     * 增强请求：在用户消息中添加重复问题的指令
     */
    private AdvisedRequest enhanceRequest(AdvisedRequest request) {
        String originalUserText = request.userText();
        
        // 使用PromptTemplate构造函数渲染增强的提示词
        PromptTemplate promptTemplate = new PromptTemplate(this.re2AdviseTemplate);
        String augmentedUserText = promptTemplate.render(Map.of("re2_input_query", originalUserText));

        // 创建新的AdvisedRequest，替换userText
        return AdvisedRequest.builder()
                .chatModel(request.chatModel())
                .userText(augmentedUserText)
                .systemText(request.systemText())
                .chatOptions(request.chatOptions())
                .build();
    }

    @Override
    public String getName() {
        return "ReReadingAdvisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public ReReadingAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }
}
