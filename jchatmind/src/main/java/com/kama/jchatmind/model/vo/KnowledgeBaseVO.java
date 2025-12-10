package com.kama.jchatmind.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeBaseVO {
    private String id;
    private String name;
    private String description;
}

