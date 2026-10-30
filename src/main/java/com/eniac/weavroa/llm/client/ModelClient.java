package com.eniac.weavroa.llm.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 通用模型客户端接口
 * 所有模型客户端的基础接口（单次请求）
 *
 * @param <Request>  请求类型
 * @param <Response> 响应类型
 */
public interface ModelClient<Request, Response> {

    /**
     * 发送请求并获取响应
     *
     * @param request 请求
     * @return 响应 Mono
     */
    Mono<Response> send(Request request, Class<Response> responseType);

    /**
     * 发送请求并获取流式响应
     *
     * @param request 请求
     * @return 响应 Flux
     */
    Flux<Response> sendStream(Request request, Class<Response> responseType);
}
