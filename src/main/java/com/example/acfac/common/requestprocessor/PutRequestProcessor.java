package com.example.acfac.common.requestprocessor;

import com.example.acfac.common.AbstractRequestProcessor;
import com.example.acfac.common.HealthCheckService;
import com.example.acfac.common.LoadBalancer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PutRequestProcessor extends AbstractRequestProcessor {

    public PutRequestProcessor(LoadBalancer loadBalancer,
        WebClient webClient,
        HealthCheckService healthCheckService) {
        super(loadBalancer, webClient, healthCheckService);
    }

    @Override
    protected Mono<String> executeRequest(String serverUrl, String requestBody) {
        return webClient.put()
            .uri(serverUrl)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2);
    }
}
