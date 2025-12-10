package com.kama.jchatmind.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kama.jchatmind.message.SseMessage;
import com.kama.jchatmind.model.dto.ChatMessageDTO;
import com.kama.jchatmind.model.dto.KnowledgeBaseDTO;
import com.kama.jchatmind.model.request.CreateChatMessageRequest;
import com.kama.jchatmind.model.response.CreateChatMessageResponse;
import com.kama.jchatmind.service.ChatMessageFacadeService;
import com.kama.jchatmind.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JChatMind {
    // 名称
    private String name;

    // 描述
    private String description;

    private String agentId;

    // 系统提示
    private String systemPrompt;

    // 模型
    private ChatModel chatModel;

    // 交互实例
    private ChatClient chatClient;

    // 状态
    private AgentState agentState;

    // 可用的工具
    private List<ToolCallback> availableTools;

    // 工具调用管理器
    private ToolCallingManager toolCallingManager;

    // 可访问的知识库
    private List<KnowledgeBaseDTO> availableKbs;

    // 当前步骤，用于实现 Agent Loop
    private int currentStep = 0;

    // 最大步骤
    private int maxSteps;

    // 模型的聊天记录
    private ChatMemory chatMemory;

    // 模型的聊天会话 ID
    private String chatSessionId;

    // 最多循环次数
    private final int MAX_STEPS = 20;

    // 最大聊天上下文长度
    private final int MAX_MESSAGES = 20;

    // SpringAI 自带的 ChatOptions, 不是 AgentDTO.ChatOptions
    private ChatOptions chatOptions;

    // SSE 服务, 用于发送消息给前端
    private SseService sseService;

    private ObjectMapper objectMapper;

    private ChatMessageFacadeService chatMessageFacadeService;

    private AgentPlan agentPlan;

    // 最后一次的 ChatResponse
    private ChatResponse lastChatResponse;

    public JChatMind() {
    }

    public JChatMind(String agentId,
                     String name,
                     String description,
                     String systemPrompt,
                     ChatModel chatModel,
                     ChatClient chatClient,
                     List<ToolCallback> availableTools,
                     List<KnowledgeBaseDTO> availableKbs,
                     String chatSessionId,
                     SseService sseService,
                     ObjectMapper objectMapper,
                     ChatMessageFacadeService chatMessageFacadeService
                     ) {

        this.agentId = agentId;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;

        this.chatModel = chatModel;
        this.chatClient = chatClient;

        this.availableTools = availableTools;
        this.availableKbs = availableKbs;

        this.chatSessionId = chatSessionId;
        this.sseService = sseService;

        this.objectMapper = objectMapper;
        this.chatMessageFacadeService = chatMessageFacadeService;

        this.agentState = AgentState.IDLE;
        this.maxSteps = MAX_STEPS;

        // 保存聊天记录
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES)
                .build();

        // 关闭 SpringAI 自带的内部的工具调用自动执行功能
        this.chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();

        // 工具调用管理器
        this.toolCallingManager = ToolCallingManager.builder().build();

        // 初始化系统提示
        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        this.chatMemory.add(chatSessionId, systemMessage);
    }

    private void changeState(AgentState agentState) {
        this.agentState = agentState;
    }

    private void changeState(AgentState agentState, Runnable callback) {
        changeState(agentState);
        callback.run();
    }

    private void answer(String userInput) {
        UserMessage userMessage = new UserMessage(userInput);

        Prompt prompt = Prompt.builder()
                .chatOptions(chatOptions)
                .messages(this.chatMemory.get(chatSessionId))
                .messages(userMessage)
                .build();

        String content = chatClient.prompt(prompt)
                .call()
                .content();
        // this is a thinking
    }

    private void fluxAnswer(String userInput) {
        UserMessage userMessage = new UserMessage(userInput);

        Prompt prompt = Prompt.builder()
                .chatOptions(chatOptions)
                .messages(this.chatMemory.get(chatSessionId))
                .messages(userMessage)
                .build();

        Flux<ChatResponse> chatResponseFlux = chatClient.prompt(prompt)
                .stream()
                .chatResponse();

        // 创建 ChatMessage
        CreateChatMessageRequest request = CreateChatMessageRequest.builder()
                .role(ChatMessageDTO.RoleType.ASSISTANT)
                .sessionId(this.chatSessionId)
                .agentId(this.agentId)
                .content("")
                .build();

        // chatMessageId
        CreateChatMessageResponse chatMessage = chatMessageFacadeService.createChatMessage(request);
        String chatMessageId = chatMessage.getChatMessageId();

        chatResponseFlux.subscribe(chatResponse -> {
            String text = chatResponse.getResult()
                    .getOutput()
                    .getText();

            SseMessage.Payload payload = SseMessage.Payload.builder()
                    .content(text)
                    .done(false)
                    .build();

            SseMessage.Metadata metadata = SseMessage.Metadata.builder()
                    .chatMessageId(chatMessageId)
                    .build();

            SseMessage message = SseMessage.builder()
                    .type(SseMessage.Type.AI_GENERATED_CONTENT)
                    .payload(payload)
                    .metadata(metadata)
                    .build();

            sseService.send(chatSessionId, message);
            chatMessageFacadeService.appendChatMessage(chatMessageId, text);
        });
    }

    private void plan(String userInput) {
        String planPrompt = """
                 你是一个任务规划助手。
                 请基于以下信息，为本次对话生成一个「多步任务规划」：
                                \s
                 - 用户初始输入：%s
                 - Agent 描述：%s
                                \s
                 【生成要求】
                 - 如果用户的初始输入中，并不包含任何可分解的任务执行，请直接用自然语言直接回答，并直接调用 directAnswer 工具。
                 - 如果用户的输入需要分解为任务执行，请严格返回要求格式的 JSON,  在返回 JSON 的情况下，生成内容中不要包含任何解释文本，只能返回合法的 JSON 字符串。
                                \s
                 【JSON 格式】
                  {
                     "steps" : [
                        {
                            "id": 1,
                            "target": "第一步的目标",
                            "detail": "第一步步骤详情，如何达到目标详细描述"
                        },
                        {
                            "id": 2,
                            "target": "第二步的目标",
                            "detail": "第二步步骤详情，如何达到目标详细描述"
                        }
                     ]
                 }
                \s""".formatted(userInput, this.description);

        Prompt prompt = Prompt.builder()
                .messages(chatMemory.get(chatSessionId))
                .chatOptions(this.chatOptions)
                .build();

        ChatResponse chatResponse = this.chatClient
                .prompt(prompt)
                .toolCallbacks(this.availableTools.toArray(new ToolCallback[0]))
                .system(planPrompt)
                .call()
                .chatClientResponse()
                .chatResponse();

        Assert.notNull(chatResponse, "ChatResponse cannot be null");

        if (chatResponse.hasToolCalls()) {
            // 如果 AI 的回答中包含工具调用，
            // 谨慎一些先判断包含的工具调用是不是 directAnswer
            boolean b = chatResponse.getResult()
                    .getOutput()
                    .getToolCalls()
                    .stream()
                    .anyMatch(tool -> tool.name().equals("directAnswer"));
            if (b) {  // 确实是 directAnswer 工具调用
                log.info("AI 选择直接回答");
                this.fluxAnswer(userInput);
                this.changeState(AgentState.FINISHED);
                return;
            } else {
                // plan 阶段中 AI 按理说只会调用 directAnswer,
                // 这里走到非 directAnswer 的分支，只能说 AI 抽风了瞎调用
                throw new RuntimeException("AI 异常工具调用");
            }
        }

        // 如果不包含工具调用，则 AI 输出了规划结果
        String content = chatResponse.getResult()
                .getOutput()
                .getText();

        // 尝试将 JSON 字符串解析成 AgentPlan 对象
        try {
            this.agentPlan = objectMapper.readValue(content, AgentPlan.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AgentPlan JSON 解析异常");
        }
    }

    // 大模型生成，接下来应该做什么
    private boolean think() {
        String thinkPrompt = """
                 现在你是一个 智能的的具体「决策模块」。
                 当前已经拥有【全局步骤规划】，你可以参考该步骤规划，如果
                 请根据当前对话上下文，决定下一步的动作。
                                \s
                 【额外信息】
                 - 你目前拥有的知识库列表以及描述：%s
                 - 全局步骤规划：%s
                                \s
                 【输出要求】
                 - 需要用自然语言说明你的选择工具调用决策理由
                 - 如果打算调用工具：请逐个返回工具调用信息，尽量不要一次性返回多个工具调用
                 - 如果你觉得
                \s""".formatted(this.availableKbs, this.agentPlan);

        // 将 thinkPrompt 通过 .user(thinkPrompt) 的方式构造进入 chatClient 中
        // 既能让每次 messageList 的最后一条是 本条提示词，
        // 又能够避免将 thinkPrompt 加入到聊天记录中
        Prompt prompt = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(this.chatMemory.get(this.chatSessionId))
                .build();

        this.lastChatResponse = this.chatClient
                .prompt(prompt)
                .user(thinkPrompt)
                .toolCallbacks(this.availableTools.toArray(new ToolCallback[0]))
                .call()
                .chatClientResponse()
                .chatResponse();

        Assert.notNull(lastChatResponse, "Last chat client response cannot be null");

        AssistantMessage output = this.lastChatResponse
                .getResult()
                .getOutput();

        List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
        String collect = toolCalls.stream()
                .map(toolCall -> String.format("Tool call: %s, arguments = %s", toolCall.name(), toolCall.arguments()))
                .collect(Collectors.joining("\n"));
        log.info("Tool calls: {}", collect);
        return !toolCalls.isEmpty();
    }

    // 执行,
    private void execute() {
        Assert.notNull(this.lastChatResponse, "Last chat client response cannot be null");

        if (!this.lastChatResponse.hasToolCalls()) {
            return;
        }

        Prompt prompt = new Prompt(this.chatMemory.get(this.chatSessionId), this.chatOptions);

        ToolExecutionResult toolExecutionResult = toolCallingManager
                .executeToolCalls(prompt, this.lastChatResponse);

        this.chatMemory.clear(this.chatSessionId);
        this.chatMemory.add(this.chatSessionId, toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);

        String collect = toolResponseMessage.getResponses()
                .stream()
                .map(resp -> "工具" + resp.name() + "的返回结果为：" + resp.responseData())
                .collect(Collectors.joining("\n"));

//        this.chatMemory.add(this.chatSessionId, toolResponseMessage);

        log.info("工具调用结果：{}", collect);

        if (toolResponseMessage.getResponses().stream().anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }
    }

    // 单个步骤模板
    private void step() {
        if (think()) {
            execute();
        }
    }

    // 运行
    public void run(String userInput) {
        Assert.notNull(userInput, "User input cannot be null");

        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent is not idle");
        }

        // 计划阶段
        this.changeState(AgentState.PLANNING);
        plan(userInput);

        UserMessage userMessage = new UserMessage(userInput);
        this.chatMemory.add(this.chatSessionId, userMessage);

        try {
            for (int i = 0; i < maxSteps && agentState != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                step();
                if (currentStep >= maxSteps) {
                    agentState = AgentState.FINISHED;
                    log.info("Agent finished running");
                }
            }
            agentState = AgentState.FINISHED;
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("Error running agent", e);
            throw new RuntimeException("Error running agent", e);
        }
    }
}
