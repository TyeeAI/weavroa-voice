package com.eniac.weavroa.llm.model.chat.request.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class JSONSchemaOption {
    /**
     * （必选）
     * Schema 的唯一标识名称。仅支持字母（不区分大小写）、数字、下划线和短横线，最长 64 个字符。
     * */
    private String name;
    /**
     * （可选）
     * 描述 Schema 的用途，帮助模型理解输出的语义上下文。
     * */
    private String description;
    /**
     * （可选）
     * 符合 JSON Schema 标准的对象，定义模型输出的数据结构。
     * */
    private Map<String, Object> schema;
    /**
     * 可选）默认值为false
     * 控制是否强制模型严格遵守 Schema 的所有约束。
     * true（推荐）
     * 模型严格遵循字段类型、必填项、格式等所有约束，确保输出 100% 合规。
     * false（不推荐）
     * 模型仅大致遵循 Schema，可能生成不符合规范的输出，导致验证失败。
     * */
    private boolean strict = true;


}
