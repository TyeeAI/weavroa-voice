package com.eniac.weavroa.llm.model.chat.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具函数调用信息
 * 表示 assistant 发起的函数调用的名称和参数
 */
@Getter
@AllArgsConstructor
public class ToolFunction {

    /**
     * 函数名称
     */
    private final String name;

    /**
     * 函数参数（JSON 字符串）
     */
    private final String arguments;
}

