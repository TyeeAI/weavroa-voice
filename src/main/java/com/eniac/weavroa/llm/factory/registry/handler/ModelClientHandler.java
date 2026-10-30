package com.eniac.weavroa.llm.factory.registry.handler;

import com.eniac.weavroa.configuration.http.WebHttpClient;
import com.eniac.weavroa.llm.config.ModelProperties;
import com.eniac.weavroa.llm.model.CapabilityModel;
import com.eniac.weavroa.llm.model.ModelCapability;

/**
 * 模型处理器接口
 * 负责根据 ModelProperties 创建对应的 CapabilityModel 实例。
 * 每种 ModelCapability 对应一个或多个 Handler 实现。
 *
 * @param <M> CapabilityModel 子类型
 */
public interface ModelClientHandler<M extends CapabilityModel> {

    /**
     * 判断是否支持处理该模型配置
     *
     * @param properties 模型配置
     * @param capability 模型能力类型
     * @return 是否支持
     */
    boolean support(ModelProperties properties, ModelCapability capability);

    /**
     * 根据 ModelProperties 创建对应的 CapabilityModel
     *
     * @param properties 模型配置
     * @param httpClient HTTP 客户端
     * @return CapabilityModel 实例
     */
    M create(ModelProperties properties, WebHttpClient httpClient);
}
