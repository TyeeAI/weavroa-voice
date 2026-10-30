package com.eniac.weavroa.llm.model;

/**
 * 能力模型接口
 * 统一封装模型信息与可调用的 Client
 *
 */
public interface CapabilityModel {

    /**
     * 获取模型唯一标识
     */
    String getId();

    /**
     * 获取模型名称
     */
    String getName();

    /**
     * 获取模型供应商
     */
    String getProvider();

    /**
     * 获取模型专长描述
     */
    String getSpecialization();

    /**
     * 获取模型能力类型
     */
    ModelCapability getCapability();
}
