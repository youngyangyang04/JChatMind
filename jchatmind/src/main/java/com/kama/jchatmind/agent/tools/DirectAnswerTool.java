package com.kama.jchatmind.agent.tools;

import org.springframework.stereotype.Component;

@Component
public class DirectAnswerTool implements Tool {

    @Override
    public String getName() {
        return "directAnswer";
    }

    @Override
    public String getDescription() {
        return "用户的问题不需要执行任务，可以直接回答";
    }

    @Override
    public ToolType getType() {
        return ToolType.FIXED;
    }

    @org.springframework.ai.tool.annotation.Tool(name = "directAnswer", description = "如果当前用户的问题无需分解成任务执行，可以直接回答时，可以调用这个工具")
    public void directAnswer() {}
}
