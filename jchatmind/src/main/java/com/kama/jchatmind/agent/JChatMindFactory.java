package com.kama.jchatmind.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.agent.tools.Tool;
import com.kama.jchatmind.converter.AgentConverter;
import com.kama.jchatmind.converter.KnowledgeBaseConverter;
import com.kama.jchatmind.mapper.AgentMapper;
import com.kama.jchatmind.mapper.KnowledgeBaseMapper;
import com.kama.jchatmind.model.dto.AgentDTO;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.entity.Agent;
import com.kama.jchatmind.model.entity.KnowledgeBase;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import com.kama.jchatmind.service.SseService;
import com.kama.jchatmind.service.ToolFacadeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JChatMindFactory {

    private final List<ChatModel> chatModels;
    private final DeepSeekChatModel chatModel;
    private final ChatClient deepSeekChatClient;
    private final SseService sseService;
    private final ObjectMapper objectMapper;
    private final AgentMapper agentMapper;
    private final AgentConverter agentConverter;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseConverter knowledgeBaseConverter;
    private final ToolFacadeService toolFacadeService;
    private final ChatMessageFacadeService chatMessageFacadeService;

    public JChatMindFactory(DeepSeekChatModel chatModel,
                            ChatClient deepSeekChatClient,
                            SseService sseService,
                            ObjectMapper objectMapper,
                            AgentMapper agentMapper,
                            AgentConverter agentConverter,
                            List<ChatModel> chatModels,
                            KnowledgeBaseMapper knowledgeBaseMapper,
                            KnowledgeBaseConverter knowledgeBaseConverter,
                            ToolFacadeService toolFacadeService,
                            ChatMessageFacadeService chatMessageFacadeService
    ) {
        this.chatModel = chatModel;
        this.deepSeekChatClient = deepSeekChatClient;
        this.sseService = sseService;
        this.objectMapper = objectMapper;
        this.agentMapper = agentMapper;
        this.agentConverter = agentConverter;
        this.chatModels = chatModels;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeBaseConverter = knowledgeBaseConverter;
        this.toolFacadeService = toolFacadeService;
        this.chatMessageFacadeService = chatMessageFacadeService;
    }

    public JChatMind create(String agentId, String chatSessionId) {
        Agent agent = agentMapper.selectById(agentId);

        // 可用工具分为两类：固定工具和可选工具
        List<Tool> availableToolList = new ArrayList<>(toolFacadeService.getFixedTools());

        List<KnowledgeBaseDTO> availableKbList = new ArrayList<>();
        List<Tool> tools = toolFacadeService.getOptionalTools();

        try {
            AgentDTO agentDTO = this.agentConverter.toDTO(agent);

            List<String> allowedKbs = agentDTO.getAllowedKbs();
            List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectByIdBatch(allowedKbs);

            for (KnowledgeBase knowledgeBase : knowledgeBases) {
                KnowledgeBaseDTO kbDTO = this.knowledgeBaseConverter.toDTO(knowledgeBase);
                availableKbList.add(kbDTO);
            }

            List<String> allowedTools = agentDTO.getAllowedTools();
            for (String toolName : allowedTools) {
                for (Tool tool : tools) {
                    if (tool.getName().equals(toolName)) {
                        availableToolList.add(tool);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 为每个工具对象单独创建 ToolCallback，使用 AopUtils 获取实际目标对象以避免 Spring 代理问题
        List<ToolCallback> toolCallbackList = new ArrayList<>();
        for (Tool tool : availableToolList) {
            try {
                // 获取实际的目标对象
                Object targetObject = AopUtils.isAopProxy(tool) ? AopUtils.getTargetClass(tool) : tool;
                ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
                        .toolObjects(targetObject)
                        .build()
                        .getToolCallbacks();
                toolCallbackList.addAll(Arrays.asList(callbacks));
            } catch (Exception e) {
                // 如果某个工具无法创建 ToolCallback，记录错误但继续处理其他工具
                throw new RuntimeException("无法为工具 " + tool.getName() + " 创建 ToolCallback", e);
            }
        }

        return new JChatMind(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getSystemPrompt(),
                chatModel,
                deepSeekChatClient,
                toolCallbackList,
                availableKbList,
                chatSessionId,
                sseService,
                objectMapper,
                chatMessageFacadeService
        );
    }
}
