package com.example.acfac.common;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public enum HttpMethodStrategy {

    GET((builder, url, request) ->
        builder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    POST((builder, url, request) ->
        builder.build()
            .post()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    PUT((builder, url, request) ->
        builder.build()
            .put()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    PATCH((builder, url, request) ->
        builder.build()
            .patch()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    ),

    DELETE((builder, url, request) ->
        builder.build()
            .delete()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2)
    );

    private final WebClientAction action;

    /**
     * WebClient 로 HTTP 요청 실행 (비동기 방식)
     * @param builder WebClient 인스턴스
     * @param url 요청 URL
     * @param request 요청 데이터
     * @return Mono<String> (비동기 응답데이터)
     */
    public Mono<String> execute(WebClient.Builder builder, String url, String request) {
        try {
            return action.apply(builder, url, request)
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
        Mono<String> apply(WebClient.Builder builder, String url, String request);
    }
}
