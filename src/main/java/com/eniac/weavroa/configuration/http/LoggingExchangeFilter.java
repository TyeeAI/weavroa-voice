package com.eniac.weavroa.configuration.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 请求日志拦截器
 * 类似 RestTemplate 的 ClientHttpRequestInterceptor
 * 在请求发送前拦截并打印请求体
 */
public class LoggingExchangeFilter implements ExchangeFilterFunction {

    private final ObjectMapper objectMapper;

    public LoggingExchangeFilter() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // 打印请求信息
        System.out.println("========== HTTP Request (Interceptor) ==========");
        System.out.println("Method: " + request.method());
        System.out.println("URL: " + request.url());
        request.headers().forEach((name, values) -> {
            if (name.equalsIgnoreCase("Authorization")) {
                System.out.println("Header: " + name + "=***");
            } else {
                values.forEach(value -> System.out.println("Header: " + name + "=" + value));
            }
        });

        // 包装 BodyInserter 以拦截请求体
        BodyInserter<?, ? super ClientHttpRequest> originalBody = request.body();
        BodyInserter<?, ? super ClientHttpRequest> loggingBody = (outputMessage, context) -> {
            LoggingClientHttpRequest loggingRequest = new LoggingClientHttpRequest(outputMessage, this.objectMapper);
            return originalBody.insert(loggingRequest, context)
                    .doOnTerminate(() -> {
                        String bodyContent = loggingRequest.getCapturedBody();
                        if (bodyContent != null && !bodyContent.isEmpty()) {
                            System.out.println("Body:\n" + bodyContent);
                        }
                        System.out.println("=================================================");
                    });
        };

        // 创建新请求
        ClientRequest newRequest = ClientRequest.from(request)
                .body(loggingBody)
                .build();

        return next.exchange(newRequest);
    }

    /**
     * 包装 ClientHttpRequest 以捕获请求体
     */
    private static class LoggingClientHttpRequest implements ClientHttpRequest {

        private final ClientHttpRequest delegate;
        private final ObjectMapper objectMapper;
        private final StringBuilder capturedBody = new StringBuilder();

        public LoggingClientHttpRequest(ClientHttpRequest delegate, ObjectMapper objectMapper) {
            this.delegate = delegate;
            this.objectMapper = objectMapper;
        }

        public String getCapturedBody() {
            return this.capturedBody.toString();
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return this.delegate.writeWith(
                    Mono.from(body).doOnNext(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        dataBuffer.readPosition(0); // 重置读取位置
                        String content = new String(bytes);
                        // 尝试格式化 JSON
                        try {
                            Object json = this.objectMapper.readValue(content, Object.class);
                            this.capturedBody.append(this.objectMapper.writeValueAsString(json));
                        } catch (JsonProcessingException e) {
                            this.capturedBody.append(content);
                        }
                    }).flux()
            );
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return this.delegate.writeAndFlushWith(body);
        }

        @Override
        public Mono<Void> setComplete() {
            return this.delegate.setComplete();
        }

        @Override
        public DataBufferFactory bufferFactory() {
            return this.delegate.bufferFactory();
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.delegate.getHeaders();
        }

        @Override
        public HttpMethod getMethod() {
            return this.delegate.getMethod();
        }

        @Override
        public URI getURI() {
            return this.delegate.getURI();
        }

        @Override
        public <T> T getNativeRequest() {
            return this.delegate.getNativeRequest();
        }

        @Override
        public MultiValueMap<String, HttpCookie> getCookies() {
            return this.delegate.getCookies();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return this.delegate.getAttributes();
        }

        @Override
        public void beforeCommit(Supplier<? extends Mono<Void>> action) {
            this.delegate.beforeCommit(action);
        }

        @Override
        public boolean isCommitted() {
            return this.delegate.isCommitted();
        }
    }
}
