package com.eniac.weavroa.llm.model.chat.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogprobContent {

    /**
     * 当前对象在choices数组中的索引。
     */
    private String token;

    /**
     * 当前 Token 的 UTF‑8 原始字节列表，用于精确还原输出内容（例如表情符号或中文字符）。
     */
    private Byte[] bytes;

    /**
     * 当前 Token 的对数概率。返回值为 null 表示概率值极低。
     */
    private float logprob;

    /**
     * 当前 Token 位置最可能的若干候选 Token，数量与请求参数top_logprobs保持一致。
     */
    @JsonProperty("top_logprobs")
    private List<LogprobContent> topLogprobs;


}
