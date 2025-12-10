package com.kama.jchatmind;

import com.kama.jchatmind.agent.JChatMindFactory;
import com.kama.jchatmind.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JChatMindTests {

    @Autowired
    JChatMindFactory jChatMindFactory;

    @Autowired
    RagService ragService;

    @Test
    public void simpleTest() {
//        JChatMind jChatMind = jChatMindFactory.create();
//        jChatMind.run("生命的意义是什么，将你的回答写在 output.txt 文件内。");
    }

    @Test
    public void simpleTest2() {
        // simple test
        ragService.similaritySearch("12f73f6a-3c1e-44c3-96d2-78211fcb3b77", "评论表");
    }
}
