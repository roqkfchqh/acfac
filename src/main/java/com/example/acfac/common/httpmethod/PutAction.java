package com.example.acfac.common.httpmethod;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PutAction implements HttpMethodAction {

    @Override
    public Mono<String> execute(WebClient.Builder builder, String url, String request) {
        return builder.build()
            .put()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2);
    }
}
