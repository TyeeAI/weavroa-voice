package com.eniac.weavroa.llm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.eniac.weavroa.llm.model.ModelCapability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型配置
 * 绑定配置前缀: weavroa.llm.model
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "weavroa.llm.model")
public class ModelConfigure {

    /**
     * 模型注册配置
     * 结构: register.能力类型 -> 模型配置列表
     */
    private Map<ModelCapability, List<ModelProperties>> register = new HashMap<>();

    public ModelConfigure() {
    }

    /**
     * 检查是否有注册的模型
     */
    public boolean hasModels() {
        return this.register != null && !this.register.isEmpty();
    }
}

