package com.serain.travelagent;

import com.serain.travelagent.app.TravelAgent;
import com.serain.travelagent.rag.TravelDocumentLoader;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelAgentApplicationTests {

    @Resource
    private TravelDocumentLoader travelDocumentLoader;

    @Test
    void contextLoads() {
        travelDocumentLoader.loadAllDocuments("document");
    }

}
