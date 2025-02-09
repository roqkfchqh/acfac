package com.example.acfac.common.requestprocessor;

import com.example.acfac.common.AbstractRequestProcessor;
import com.example.acfac.common.HealthCheckService;
import com.example.acfac.common.LoadBalancer;
import com.example.acfac.kafka.KafkaRequestProducer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GetRequestProcessor extends AbstractRequestProcessor {

    public GetRequestProcessor(LoadBalancer loadBalancer,
        KafkaRequestProducer kafkaRequestProducer,
        WebClient webClient,
        HealthCheckService healthCheckService) {
        super(loadBalancer, kafkaRequestProducer, webClient, healthCheckService);
    }

    @Override
    protected Mono<String> executeRequest(String serverUrl, String requestBody) {
        return webClient.get()
            .uri(serverUrl)
            .retrieve()
            .bodyToMono(String.class)
            .retry(2);
    }
}
