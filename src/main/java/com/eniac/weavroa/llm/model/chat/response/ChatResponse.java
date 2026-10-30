package com.eniac.weavroa.llm.model.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话响应
 * OpenAI API 格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 本次调用的唯一标识符。
     */
    private String id;

    /**
     * 模型生成内容的数组。
     */
    private List<Choice> choices;

    /**
     * 请求创建时的 Unix 时间戳（秒）。
     */
    private Long created;

    /**
     * 本次请求使用的模型。
     */
    private String model;

    /**
     * 响应对象类型
     */
    private String object;

    /**
     * Token 使用统计
     */
    private Usage usage;

    private String system_fingerprint;

}
