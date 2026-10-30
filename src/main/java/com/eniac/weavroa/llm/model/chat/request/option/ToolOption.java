package com.eniac.weavroa.llm.model.chat.request.option;

import com.eniac.weavroa.llm.model.chat.tool.ToolCall;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工具调用控制参数
 * 用于配置 Function Calling 功能
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolOption {

    /**
     * 默认配置（无工具）
     */
    public static final ToolOption DEFAULT = ToolOption.builder().build();

    /**
     * 工具定义列表
     * 包含一个或多个工具对象，供模型在 Function Calling 中调用
     */
    private List<ToolCall> tools;

    /**
     * 工具选择策略
     * 可选值：
     * - auto: 大模型自主选择工具策略
     * - none: 禁用工具调用
     * - {"type": "function", "function": {"name": "xxx"}}: 强制调用指定工具
     */
    @Builder.Default
    @JsonProperty("tool_choice")
    private String toolChoice = "auto";

    /**
     * 是否开启并行工具调用
     */
    @JsonProperty("parallel_tool_calls")
    private Boolean parallelToolCalls;

    /**
     * 创建指定工具列表的选项（自动选择策略）
     */
    public static ToolOption of(List<ToolCall> tools) {
        return ToolOption.builder()
                .tools(tools)
                .toolChoice("auto")
                .build();
    }

    /**
     * 创建指定工具列表的选项（禁用工具调用）
     */
    public static ToolOption none() {
        return ToolOption.builder()
                .toolChoice("none")
                .build();
    }

    /**
     * 创建强制调用指定工具的选项
     */
    public static ToolOption required(List<ToolCall> tools, String functionName) {
        return ToolOption.builder()
                .tools(tools)
                .toolChoice("{\"type\": \"function\", \"function\": {\"name\": \"" + functionName + "\"}}")
                .build();
    }

    /**
     * 创建并行工具调用的选项
     */
    public static ToolOption parallel(List<ToolCall> tools) {
        return ToolOption.builder()
                .tools(tools)
                .toolChoice("auto")
                .parallelToolCalls(true)
                .build();
    }
}
