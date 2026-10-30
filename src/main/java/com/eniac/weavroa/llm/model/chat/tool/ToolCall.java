package com.eniac.weavroa.llm.model.chat.tool;

import lombok.Getter;

/**
 * 工具调用
 * 表示 assistant 发起的一次工具调用请求
 */
@Getter
public class ToolCall {

    /**
     * 工具调用 ID
     */
    private final String id;

    /**
     * 工具调用类型
     */
    private final String type = "function";

    /**
     * 工具调用索引（用于流式增量拼接）
     */
    private final Integer index;

    /**
     * 函数调用信息
     */
    private final ToolFunction function;

    public ToolCall(String id, Integer index, String name, String arguments) {
        this.id = id;
        this.index = index;
        this.function = new ToolFunction(name, arguments);
    }
}
