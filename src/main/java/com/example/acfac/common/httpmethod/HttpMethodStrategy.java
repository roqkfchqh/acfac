package com.example.acfac.common.httpmethod;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public enum HttpMethodStrategy {

    GET(new GetAction()),
    POST(new PostAction()),
    PUT(new PutAction()),
    PATCH(new PatchAction()),
    DELETE(new DeleteAction());

    private final HttpMethodAction action;

    public Mono<String> execute(WebClient.Builder builder, String url, String request) {
        return action.execute(builder, url, request)
            .onErrorMap(WebClientResponseException.class, e ->
                new RuntimeException("HTTP 에러: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e)
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("HTTP 요청을 처리하는 데 실패했습니다", e)));
    }
}