package com.example.acfac.common;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public enum HttpMethodStrategy {

    GET((client, url, request) ->
        client.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    POST((client, url, request) ->
        client.post()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    PUT((client, url, request) ->
        client.put()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    PATCH((client, url, request) ->
        client.patch()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    DELETE((client, url, request) ->
        client.delete()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    );

    private final WebClientAction action;

    /**
     * WebClient 로 HTTP 요청 실행 (비동기 방식)
     * @param client WebClient 인스턴스
     * @param url 요청 URL
     * @param request 요청 데이터
     * @return Mono<String> (비동기 응답데이터)
     */
    public Mono<String> execute(WebClient client, String url, String request) {
        try {
            return action.apply(client, url, request)
                .onErrorMap(WebClientResponseException.class, e ->
                    new RuntimeException("HTTP 에러: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e)
                )
                .onErrorResume(e -> Mono.error(new RuntimeException("HTTP 요청을 처리하는 데 실패했습니다", e)));
        } catch (Exception e) {
            return Mono.error(new RuntimeException("HTTP 요청을 처리하는 데 실패했습니다", e));
        }
    }

    @FunctionalInterface
    interface WebClientAction {
        Mono<String> apply(WebClient client, String url, String request);
    }
}
