package com.eniac.weavroa.llm.model.chat.request.option;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 思考模式控制参数
 * 用于开启和配置模型的深度思考能力
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThinkingOption {

    /**
     * 默认配置（不开启思考模式）
     */
    public static final ThinkingOption DEFAULT = ThinkingOption.builder().build();

    /**
     * 开启思考模式（使用默认 budget）
     */
    public static final ThinkingOption ENABLED = ThinkingOption.builder()
            .enableThinking(true)
            .build();

    /**
     * 是否开启思考模式
     * 使用混合思考模型时，开启后思考内容将通过 reasoning_content 字段返回
     */
    @Builder.Default
    @JsonProperty("enable_thinking")
    private boolean enableThinking = false;

    /**
     * 思考过程的最大 Token 数
     * 限制模型思考阶段消耗的 token 数量
     */
    @JsonProperty("thinking_budget")
    private Integer thinkingBudget;

    /**
     * 创建指定 budget 的思考选项
     */
    public static ThinkingOption withBudget(int budget) {
        return ThinkingOption.builder()
                .enableThinking(true)
                .thinkingBudget(budget)
                .build();
    }
}
