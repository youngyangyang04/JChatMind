package com.kama.jchatmind.agent.tools;

// 基础的工具，比如直接回答工具，终止任务工具，是所有 Agent 都必须拥有的
// 可选的工具，比如数据库查询工具，文件系统操作工具，这些工具可以根据对 Agent 的要求自由选择
public enum ToolType {
    FIXED,  // 固定拥有的工具
    OPTIONAL, // 可以选择工具
}
