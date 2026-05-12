package com.serain.travelagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}