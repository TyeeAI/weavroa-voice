package com.eniac.weavroa.llm.factory;

import com.eniac.weavroa.llm.config.ModelConfigure;
import com.eniac.weavroa.llm.config.ModelProperties;
import com.eniac.weavroa.llm.factory.registry.ModelRegistry;
import com.eniac.weavroa.llm.model.*;
import com.eniac.weavroa.llm.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 模型工厂
 * 采用类型分离存储，直接返回具体类型，无需强制转换
 */
@Slf4j
public class ModelFactory {

    private final ModelRegistry registry;
    private final ModelConfigure configure;
    // 按能力分配
    private static final Map<ModelCapability, List<CapabilityModel>> modelsCache = new ConcurrentHashMap<>();

    public ModelFactory(ModelRegistry registry, ModelConfigure  configure) {
        this.registry = registry;
        this.configure = configure;
    }

    public void init() {

        if (!this.configure.hasModels()) {
            return;
        }

        Map<ModelCapability, List<ModelProperties>> modelProperties = this.configure.getRegister();

        for (Map.Entry<ModelCapability, List<ModelProperties>> entry : modelProperties.entrySet()) {

            ModelCapability capability = entry.getKey();

            for (ModelProperties properties : entry.getValue()) {

                CapabilityModel model = this.registry.register(properties, capability);

                modelsCache.computeIfAbsent(capability, k -> new ArrayList<>()).add(model);
            }
        }
    }

    private <T extends CapabilityModel> T getModel(ModelCapability capability, Class<T> modelClass) {

        List<CapabilityModel> models = modelsCache.get(capability);
        // TODO 后续可用决策模式选择一个合适的模型
        CapabilityModel model = models.getFirst();

        return modelClass.cast(model);
    }

    public ChatModel chatModel() {
        return this.getModel(ModelCapability.TEXT, ChatModel.class);
    }

}
