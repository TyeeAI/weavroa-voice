package com.eniac.weavroa.configuration.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 统一 HTTP 客户端
 * 封装 WebClient，提供简洁的 HTTP 请求方法
 */
public class WebHttpClient {

    private final WebClient webClient;

    public WebHttpClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> get(String url, Map<String, String> headers, Class<T> responseType) {
        return this.webClient
                .get()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Flux<T> getStream(String url, Map<String, String> headers, Class<T> responseType) {
        return this.webClient
                .get()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(responseType);
    }

    public <T> Mono<T> post(String url, Map<String, String> headers, Object body, Class<T> responseType) {
        return this.webClient
                .post()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Flux<T> postStream(String url, Map<String, String> headers, Object body, Class<T> responseType) {
        ObjectMapper om = new ObjectMapper();

        return this.webClient
                .post()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(s -> !s.isEmpty())
                .filter(s-> !s.contains("[DONE]"))
                .map(s-> {
                    try {
                        return om.readValue(s, responseType);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
//                .bodyToFlux(responseType);
    }

    public <T> Mono<T> put(String url, Object body, Class<T> responseType) {
        return this.put(url, null, body, responseType);
    }

    public <T> Mono<T> put(String url, Map<String, String> headers, Object body, Class<T> responseType) {
        return this.webClient
                .put()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Mono<T> delete(String url, Class<T> responseType) {
        return this.delete(url, null, responseType);
    }

    public <T> Mono<T> delete(String url, Map<String, String> headers, Class<T> responseType) {
        return this.webClient
                .delete()
                .uri(url)
                .headers(h -> this.applyHeaders(h, headers))
                .retrieve()
                .bodyToMono(responseType);
    }

    private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
    }
}
