package com.kama.jchatmind.service;

import com.kama.jchatmind.message.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    // 没有用户系统，使用 chatSessionId 作为连接标识
    SseEmitter connect(String chatSessionId);

    void send(String chatSessionId, SseMessage message);
}
