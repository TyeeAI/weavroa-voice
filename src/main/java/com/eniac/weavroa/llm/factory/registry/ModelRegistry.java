package com.eniac.weavroa.llm.factory.registry;

import com.eniac.weavroa.llm.config.ModelProperties;
import com.eniac.weavroa.llm.model.CapabilityModel;
import com.eniac.weavroa.llm.model.ModelCapability;
import com.eniac.weavroa.llm.factory.registry.handler.ModelClientHandler;

/**
 * 模型注册器接口
 * 负责根据配置创建对应的 CapabilityModel 实例。
 * 支持动态注册 Handler 以扩展新的模型能力类型。
 */
public interface ModelRegistry {

    /**
     * 注册模型
     *
     * @param properties 模型配置
     * @param capability 模型能力类型
     * @return 创建的 CapabilityModel 实例
     */
    CapabilityModel register(ModelProperties properties, ModelCapability capability);

    /**
     * 添加模型客户端处理器
     * 允许动态添加新的模型类型支持
     *
     * @param handler 处理器实例
     */
    void addHandler(ModelClientHandler<?> handler);
}
