package com.kama.jchatmind.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.entity.ChatMessage;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.request.UpdateChatMessageRequest;
import com.kama.jchatmind.model.vo.ChatMessageVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@AllArgsConstructor
public class ChatMessageConverter {

    private final ObjectMapper objectMapper;

    public ChatMessage toEntity(ChatMessageDTO chatMessageDTO) throws JsonProcessingException {
        Assert.notNull(chatMessageDTO, "ChatMessageDTO cannot be null");
        Assert.notNull(chatMessageDTO.getRole(), "Role cannot be null");

        return ChatMessage.builder()
                .id(chatMessageDTO.getId())
                .sessionId(chatMessageDTO.getSessionId())
                .role(chatMessageDTO.getRole().getRole())
                .content(chatMessageDTO.getContent())
                .metadata(chatMessageDTO.getMetadata() != null 
                        ? objectMapper.writeValueAsString(chatMessageDTO.getMetadata()) 
                        : null)
                .createdAt(chatMessageDTO.getCreatedAt())
                .updatedAt(chatMessageDTO.getUpdatedAt())
                .build();
    }

    public ChatMessageDTO toDTO(ChatMessage chatMessage) throws JsonProcessingException {
        Assert.notNull(chatMessage, "ChatMessage cannot be null");
        Assert.notNull(chatMessage.getRole(), "Role cannot be null");

        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .sessionId(chatMessage.getSessionId())
                .role(ChatMessageDTO.RoleType.fromRole(chatMessage.getRole()))
                .content(chatMessage.getContent())
                .metadata(chatMessage.getMetadata() != null 
                        ? objectMapper.readValue(chatMessage.getMetadata(), ChatMessageDTO.MetaData.class) 
                        : null)
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .build();
    }

    public ChatMessageVO toVO(ChatMessageDTO dto) {
        return ChatMessageVO.builder()
                .id(dto.getId())
                .sessionId(dto.getSessionId())
                .role(dto.getRole())
                .content(dto.getContent())
                .metadata(dto.getMetadata())
                .build();
    }

    public ChatMessageVO toVO(ChatMessage chatMessage) throws JsonProcessingException {
        return toVO(toDTO(chatMessage));
    }

    public ChatMessageDTO toDTO(CreateChatMessageRequest request) {
        Assert.notNull(request, "CreateChatMessageRequest cannot be null");
        Assert.notNull(request.getSessionId(), "SessionId cannot be null");
        Assert.notNull(request.getRole(), "Role cannot be null");

        return ChatMessageDTO.builder()
                .sessionId(request.getSessionId())
                .role(request.getRole())
                .content(request.getContent())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateDTOFromRequest(ChatMessageDTO dto, UpdateChatMessageRequest request) {
        Assert.notNull(dto, "ChatMessageDTO cannot be null");
        Assert.notNull(request, "UpdateChatMessageRequest cannot be null");

        if (request.getContent() != null) {
            dto.setContent(request.getContent());
        }
        if (request.getMetadata() != null) {
            dto.setMetadata(request.getMetadata());
        }
    }
}
