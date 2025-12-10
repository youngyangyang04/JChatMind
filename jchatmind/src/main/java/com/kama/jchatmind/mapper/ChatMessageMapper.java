package com.kama.jchatmind.mapper;

import com.kama.jchatmind.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author charon
 * @description 针对表【chat_message】的数据库操作Mapper
 * @createDate 2025-12-02 15:40:13
 * @Entity com.kama.jchatmind.model.entity.ChatMessage
 */
@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessage chatMessage);

    ChatMessage selectById(String id);

    List<ChatMessage> selectBySessionId(String sessionId);

    int deleteById(String id);

    int updateById(ChatMessage chatMessage);
}
