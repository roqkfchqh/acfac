package com.example.acfac.common;

import com.example.acfac.values.LoadBalancerConfigProperties;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckService {   // TODO 서킷브레이커 설정 필요

    private final LoadBalancerConfigProperties configProperties;
    private final WebClient webClient;
    private final ConcurrentMap<String, Boolean> healthyServers = new ConcurrentHashMap<>();

    public List<String> getHealthyServers() {
        return healthyServers.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .toList();
    }

    @Scheduled(fixedRateString = "${healthcheck.fixedRate}")
    public void performHealthCheck() {
        List<String> allServers = configProperties.getServers();

        Flux.fromIterable(allServers)
            .flatMap(server -> isServerHealthyAsync(server)
                .map(isHealthy -> Map.entry(server, isHealthy))
            )
            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
            .subscribe(healthyMap -> {
                healthyServers.putAll(healthyMap);
                log.info("Updated healthy servers: {}", getHealthyServers());
            });
    }

    public Mono<Integer> getResponseTime(String serverUrl) {
        return checkServerHealthAndResponseTime(serverUrl)
            .map(Map.Entry::getValue);
    }

    private Mono<Boolean> isServerHealthyAsync(String serverUrl) {
        return checkServerHealthAndResponseTime(serverUrl)
            .map(Map.Entry::getKey);
    }

    private Mono<SimpleEntry<Boolean, Integer>> checkServerHealthAndResponseTime(String serverUrl) {
        long startTime = System.currentTimeMillis();

        return webClient.get()
            .uri(serverUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(3))
            .then(Mono.fromCallable(() ->
                new AbstractMap.SimpleEntry<>(true, (int) (System.currentTimeMillis() - startTime))
            ))
            .onErrorResume(e -> {
                log.error("Health check failed for {}: {}", serverUrl, e.getMessage());
                return Mono.just(new AbstractMap.SimpleEntry<>(false, configProperties.getMaxResponseTime()));
            });
    }
}
