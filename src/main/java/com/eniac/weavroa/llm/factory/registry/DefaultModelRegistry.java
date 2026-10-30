package com.eniac.weavroa.llm.factory.registry;

import com.eniac.weavroa.configuration.http.WebHttpClient;
import com.eniac.weavroa.llm.config.ModelProperties;
import com.eniac.weavroa.llm.model.CapabilityModel;
import com.eniac.weavroa.llm.model.ModelCapability;
import com.eniac.weavroa.llm.factory.registry.handler.ChatModelHandler;
import com.eniac.weavroa.llm.factory.registry.handler.ModelClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认模型注册器实现
 * 使用 Handler 注册机制支持多种模型能力类型的扩展
 */
@Slf4j
public class DefaultModelRegistry implements ModelRegistry {

    private final WebHttpClient httpClient;
    private final List<ModelClientHandler<?>> handlers = new ArrayList<>();

    public DefaultModelRegistry(WebHttpClient httpClient) {
        this.httpClient = httpClient;
        this.registerDefaultHandlers();
    }

    /**
     * 注册所有默认处理器
     */
    private void registerDefaultHandlers() {
        this.addHandler(new ChatModelHandler());
    }

    @Override
    public void addHandler(ModelClientHandler<?> handler) {
        this.handlers.add(handler);
        log.debug("注册模型处理器: {}", handler.getClass().getSimpleName());
    }

    @Override
    public CapabilityModel register(ModelProperties properties, ModelCapability capability) {
        for (ModelClientHandler<?> handler : this.handlers) {
            if (handler.support(properties, capability)) {
                return handler.create(properties, this.httpClient);
            }
        }
        throw new UnsupportedOperationException("暂不支持的模型类型: " + capability);
    }
}
