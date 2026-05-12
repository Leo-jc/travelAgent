package com.serain.travelagent.app.advisor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * @BelongsProject: travelAgent
 * @BelongsPackage: com.serain.travelagent.app.advisor
 * @Author: Serain
 * @CreateTime: 2026-05-11  20:58
 * @Description: 简单日志Advisor - 记录请求和响应
 * @Version: 1.0
 */
@Slf4j
public class SimpleLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        logRequest(advisedRequest);

        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        logResponse(advisedResponse);

        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        logRequest(advisedRequest);

        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::logResponse);
    }

    private void logRequest(AdvisedRequest request) {
        logger.info("request: {}", request.userText());
    }

    private void logResponse(AdvisedResponse response) {
        logger.info("response: {}", response.response().getResult().getOutput().getText());
    }

}
