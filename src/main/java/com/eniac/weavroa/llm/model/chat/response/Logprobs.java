package com.eniac.weavroa.llm.model.chat.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Logprobs {

    /**
     * 模型输出的 Token 概率信息。。
     */
    private List<LogprobContent> content;
}
