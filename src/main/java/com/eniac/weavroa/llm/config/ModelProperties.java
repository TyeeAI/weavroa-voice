package com.eniac.weavroa.llm.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 单个模型的配置属性
 * 对应 YAML 中每个模型的配置项
 */
@Setter
@Getter
public class ModelProperties {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 供应商（如: bailian, openai, deepseek）
     */
    private String provider;

    /**
     * API 基础地址
     */
    private String baseUrl;

    /**
     * 对接方式（如: openai）
     */
    private String apiType = "openai";

    /**
     * 模型专长描述
     */
    private String specialization;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 向量维度（Embedding模型使用）
     */
    private int hiddenSize = 0;

    /**
     * 是否支持流式输出
     */
    private boolean supportsStreaming = true;

    /**
     * 是否支持工具调用
     */
    private boolean supportsToolCalls = false;

    /**
     * 是否支持视觉（多模态）
     */
    private boolean supportsVision = false;

    /**
     * 最大上下文长度
     */
    private int maxContextLength = 8192;

    /**
     * 最大输出长度
     */
    private int maxOutputLength = 4096;

    public ModelProperties() {
    }

    @Override
    public String toString() {
        return "ModelProperties{" +
                "name='" + this.name + '\'' +
                ", provider='" + this.provider + '\'' +
                ", apiType='" + this.apiType + '\'' +
                '}';
    }
}
