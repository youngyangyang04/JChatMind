package com.kama.jchatmind.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSessionVO {
    private String id;
    private String agentId;
    private String title;
}
