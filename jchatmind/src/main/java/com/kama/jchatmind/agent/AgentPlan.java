package com.kama.jchatmind.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
public class AgentPlan {

    Step[] steps;

    @Data
    @AllArgsConstructor
    @Builder
    public static class Step {
        // 步骤编号
        String id;
        String target; // 步骤目标
        String detail; // 步骤详情
    }
}
