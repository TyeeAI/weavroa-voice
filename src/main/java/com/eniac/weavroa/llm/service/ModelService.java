package com.eniac.weavroa.llm.service;

import com.eniac.weavroa.llm.factory.ModelFactory;
import com.eniac.weavroa.llm.model.chat.Message;
import com.eniac.weavroa.llm.model.chat.request.option.GenerateOption;
import com.eniac.weavroa.llm.model.chat.request.option.ResultOption;
import com.eniac.weavroa.llm.model.chat.response.ChatResponse;
import com.eniac.weavroa.llm.model.chat.response.Choice;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * 模型响应封装服务
 * 基于认知科学中的响应处理理论，对模型调用返回的原始响应进行封装和处理
 * 提供统一的响应提取、验证和转换能力，简化上层调用
 */
@Slf4j
public class ModelService {

    private final ModelFactory modelFactory;

    public ModelService(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    /**
     * 生成第一个响应消息
     * 从ChatResponse中提取第一个Choice的Message，简化调用流程
     *
     * @param messages     输入消息列表
     * @param genOption    生成选项
     * @param resultOption 结果选项
     * @return 第一个响应消息
     */
    public Mono<Message> chatFirst(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {
        return this.doChat(messages, genOption, resultOption)
                .next();
    }

    public Mono<Message> chatFirst(List<Message> messages, GenerateOption genOption) {
        return this.doChat(messages, genOption, null)
                .next();
    }

    public Mono<Message> chatFirst(List<Message> messages) {
        return this.doChat(messages, null, null)
                .next();
    }

    public Mono<List<Message>> chat(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {
        return this.doChat(messages, genOption, resultOption)
                .collectList();
    }

    public Mono<List<Message>> chat(List<Message> messages, GenerateOption genOption) {
        return this.doChat(messages, genOption, null)
                .collectList();
    }

    public Mono<List<Message>> chat(List<Message> messages) {
        return this.doChat(messages, null, null)
                .collectList();
    }

    /**
     * 生成所有响应消息列表
     * 从ChatResponse中提取所有Choice的Message列表
     *
     * @param messages     输入消息列表
     * @param genOption    生成选项
     * @param resultOption 结果选项
     * @return 响应消息列表
     */
    private Flux<Message> doChat(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {
        return this.modelFactory
                .chatModel()
                .generate(messages, genOption, resultOption)
                .flatMapIterable(ChatResponse::getChoices)
                .map(Choice::getMessage)
                .filter(Objects::nonNull);
    }
}
