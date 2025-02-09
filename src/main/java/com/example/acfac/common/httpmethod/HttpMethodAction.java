package com.example.acfac.common.httpmethod;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public interface HttpMethodAction {

    Mono<String> execute(WebClient.Builder builder, String url, String request);
}
