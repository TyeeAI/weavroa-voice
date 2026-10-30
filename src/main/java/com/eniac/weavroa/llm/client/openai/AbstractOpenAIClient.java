package com.eniac.weavroa.llm.client.openai;

import com.eniac.weavroa.configuration.http.WebHttpClient;
import com.eniac.weavroa.llm.client.ModelClient;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * OpenAI 兼容客户端的抽象基类
 * 封装 OpenAI API 的 HTTP 请求方式，同时支持单次请求和流式请求
 *
 * @param <Request> 请求体类型
 * @param <Response> 响应体类型
 */
@Getter
@SuperBuilder
public abstract class AbstractOpenAIClient<Request, Response> implements ModelClient<Request, Response> {

    /**
     * API URL（包含完整路径）
     */
    protected String baseUrl;

    /**
     * API 密钥
     */
    protected String apiKey;

    /**
     * HTTP 客户端
     */
    protected WebHttpClient httpClient;

    /**
     * 发送 POST 请求
     */
    @Override
    public Mono<Response> send(Request request, Class<Response> responseType) {
        return this.httpClient.post(this.baseUrl, this.getHeaders(), request, responseType);
    }

    /**
     * 发送流式 POST 请求
     */
    @Override
    public Flux<Response> sendStream(Request request, Class<Response> responseType) {
        return this.httpClient.postStream(this.baseUrl, this.getHeaders(), request, responseType);
    }

    /**
     * 构建请求头
     */
    protected Map<String, String> getHeaders() {
        return Map.of("Authorization", "Bearer " + this.apiKey);
    }
}
