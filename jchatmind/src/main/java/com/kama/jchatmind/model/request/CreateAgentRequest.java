package com.kama.jchatmind.model.request;

import com.kama.jchatmind.model.dto.AgentDTO;
import lombok.Data;

import java.util.List;

@Data
public class CreateAgentRequest {
    private String name;
    private String description;
    private String systemPrompt;
    private String model;
    private List<String> allowedTools;
    private List<String> allowedKbs;
    private AgentDTO.ChatOptions chatOptions;
}
