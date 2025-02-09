package com.example.acfac.common.httpmethod;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DeleteAction implements HttpMethodAction {

    @Override
    public Mono<String> execute(WebClient.Builder builder, String url, String request) {
        return builder.build()
            .delete()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2);
    }
}

