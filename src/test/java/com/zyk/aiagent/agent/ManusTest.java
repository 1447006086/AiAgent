package com.zyk.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManusTest {

    @Resource
    private Manus manus;

    @Test
    public void test() {
        String userPrompt = """  
                帮我找两个重庆适合徒步的地点，  
                并分别为这两个地点制定徒步攻略，  
                并以 PDF 格式输出""";
        String run = manus.run(userPrompt);
        Assertions.assertNotNull(run);
    }
}