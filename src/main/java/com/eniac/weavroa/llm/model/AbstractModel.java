package com.eniac.weavroa.llm.model;

import com.eniac.weavroa.llm.client.openai.AbstractOpenAIClient;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 模型抽象基类
 * 继承 AbstractOpenAIClient 获得 HTTP 请求能力（同时支持单次和流式）
 * 实现 CapabilityModel 接口提供模型元信息
 *
 * @param <Request> 请求体类型
 * @param <Response> 响应体类型
 */
@Getter
@SuperBuilder
public abstract class AbstractModel<Request, Response> extends AbstractOpenAIClient<Request, Response> implements CapabilityModel {

    /**
     * 模型唯一标识
     */
    private String id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型供应商
     */
    private String provider;

    /**
     * 模型专长描述
     */
    private String specialization;

    /**
     * 模型能力类型
     */
    private ModelCapability capability;
}
