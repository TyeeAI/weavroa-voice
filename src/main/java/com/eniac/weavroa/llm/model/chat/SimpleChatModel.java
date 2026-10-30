package com.eniac.weavroa.llm.model.chat;

import com.eniac.weavroa.llm.model.AbstractModel;
import com.eniac.weavroa.llm.model.chat.request.ChatRequest;
import com.eniac.weavroa.llm.model.chat.request.option.GenerateOption;
import com.eniac.weavroa.llm.model.chat.request.option.ResultOption;
import com.eniac.weavroa.llm.model.chat.response.ChatResponse;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 简单对话模型实现
 * 基于 OpenAI 兼容 API
 */
@Getter
@SuperBuilder
public class SimpleChatModel extends AbstractModel<ChatRequest, ChatResponse> implements ChatModel {

    @Override
    public Mono<ChatResponse> generate(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {

        ChatRequest request = ChatRequest.of(messages, genOption, resultOption);

        request.setModel(this.getName());

        return this.send(request, ChatResponse.class);
    }

    @Override
    public Flux<ChatResponse> generateStream(List<Message> messages, GenerateOption genOption, ResultOption resultOption) {

        ChatRequest request = ChatRequest.ofStream(messages, genOption, resultOption);

        request.setModel(this.getName());

        return this.sendStream(request, ChatResponse.class);
    }
}
