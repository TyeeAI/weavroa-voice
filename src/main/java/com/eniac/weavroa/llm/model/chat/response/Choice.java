package com.eniac.weavroa.llm.model.chat.response;

import com.eniac.weavroa.llm.model.chat.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Choice {

    /**
     * 模型停止生成的原因。
     */
    @JsonProperty("finish_reason")
    private String finishReason;

    /**
     * 当前对象在choices数组中的索引。
     */
    private Integer index;

    /**
     * 模型输出的 Token 概率信息。
     */
    private Logprobs logprobs;

    /**
     * 模型输出的消息。
     * 反序列化：支持 message 和 delta
     * 序列化：只输出 message
     */
    private Message message;

    /**
     * 反序列化时将 delta 映射到 message
     */
    @JsonSetter("delta")
    public void setDelta(Message delta) {
        if (delta != null) {
            this.message = delta;
        }
    }
}
