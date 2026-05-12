package com.serain.travelagent.app;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TravelAgent implements CommandLineRunner {

    @Resource
    private ChatClient chatClient;

    @Override
    public void run(String... args) throws Exception {
        String response = chatClient.prompt()
                .user("你好，请介绍一下你自己")
                .call()
                .content();
        System.out.println("AI 回复：" + response);
    }
}
