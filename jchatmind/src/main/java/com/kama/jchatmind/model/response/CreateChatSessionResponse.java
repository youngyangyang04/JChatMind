package com.kama.jchatmind.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateChatSessionResponse {
    private String chatSessionId;
}
