package com.eniac.weavroa.llm.factory.registry.handler;

import com.eniac.weavroa.configuration.http.WebHttpClient;
import com.eniac.weavroa.llm.config.ModelProperties;
import com.eniac.weavroa.llm.model.chat.ChatModel;
import com.eniac.weavroa.llm.model.ModelCapability;
import com.eniac.weavroa.llm.model.chat.SimpleChatModel;

/**
 * TEXT 类型模型处理器
 * 负责创建对话类型的 ChatModel
 */
public class ChatModelHandler implements ModelClientHandler<ChatModel> {

    @Override
    public boolean support(ModelProperties properties, ModelCapability capability) {
        return capability == ModelCapability.TEXT;
    }

    @Override
    public ChatModel create(ModelProperties properties, WebHttpClient httpClient) {
        return switch (properties.getApiType()) {
            case "openai" -> this.createOpenAIModel(properties, httpClient);
            default -> throw new UnsupportedOperationException("不支持的对接方式: " + properties.getApiType());
        };
    }

    private ChatModel createOpenAIModel(ModelProperties properties, WebHttpClient httpClient) {
        return SimpleChatModel.builder()
                .id(properties.getProvider() + ":" + properties.getName())
                .name(properties.getName())
                .provider(properties.getProvider())
                .specialization(properties.getSpecialization())
                .capability(ModelCapability.TEXT)
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .httpClient(httpClient)
                .build();
    }
}
