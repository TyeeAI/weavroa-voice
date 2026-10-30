package com.eniac.weavroa.llm.model.chat;

import com.eniac.weavroa.llm.model.CapabilityModel;
import com.eniac.weavroa.llm.model.chat.request.option.GenerateOption;
import com.eniac.weavroa.llm.model.chat.request.option.ResultOption;
import com.eniac.weavroa.llm.model.chat.response.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 对话模型接口
 * 定义对话能力的契约
 */
public interface ChatModel extends CapabilityModel {

    /**
     * 单次对话请求（完整参数）
     */
    Mono<ChatResponse> generate(List<Message> messages, GenerateOption genOption, ResultOption resultOption);

    /**
     * 流式对话请求（完整参数）
     */
    Flux<ChatResponse> generateStream(List<Message> messages, GenerateOption genOption, ResultOption resultOption);

    /**
     * 单次对话（仅生成选项）
     */
    default Mono<ChatResponse> generate(List<Message> messages, GenerateOption genOption) {
        return this.generate(messages, genOption, null);
    }

    /**
     * 单次对话（仅结果选项）
     */
    default Mono<ChatResponse> generate(List<Message> messages, ResultOption resultOption) {
        return this.generate(messages, null, resultOption);
    }

    /**
     * 单次对话（默认选项）
     */
    default Mono<ChatResponse> generate(List<Message> messages) {
        return this.generate(messages, GenerateOption.DEFAULT, ResultOption.DEFAULT);
    }

    /**
     * 流式对话（仅生成选项）
     */
    default Flux<ChatResponse> generateStream(List<Message> messages, GenerateOption genOption) {
        return this.generateStream(messages, genOption, null);
    }

    /**
     * 流式对话（仅结果选项）
     */
    default Flux<ChatResponse> generateStream(List<Message> messages, ResultOption resultOption) {
        return this.generateStream(messages, null, resultOption);
    }

    /**
     * 流式对话（默认选项）
     */
    default Flux<ChatResponse> generateStream(List<Message> messages) {
        return this.generateStream(messages, GenerateOption.DEFAULT, ResultOption.DEFAULT);
    }
}
