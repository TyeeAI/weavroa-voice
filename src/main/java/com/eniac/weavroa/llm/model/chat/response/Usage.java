package com.eniac.weavroa.llm.model.chat.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 使用统计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usage {
    /**
     * 输入 Token 数。
     */
    @JsonProperty("prompt_tokens")
    private int promptTokens;

    /**
     * 模型输出的 Token 数。
     */
    @JsonProperty("completion_tokens")
    private int completionTokens;

    /**
     * 总 Token 数，为prompt_tokens与completion_tokens的总和。
     */
    @JsonProperty("total_tokens")
    private int totalTokens;

}