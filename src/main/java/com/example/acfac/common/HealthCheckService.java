package com.example.acfac.common;

import com.example.acfac.values.LoadBalancerConfigProperties;
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

    private Mono<Boolean> isServerHealthyAsync(String serverUrl) {
        return webClient.get()
            .uri(serverUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(3)) // 3초 이상 응답 없으면 타임아웃
            .then(Mono.fromCallable(() -> true))
            .onErrorResume(e -> {
                log.error("Health check failed for {}: {}", serverUrl, e.getMessage());
                return Mono.just(false);
            });
    }

    public Mono<Integer> getResponseTime(String serverUrl) {
        long startTime = System.currentTimeMillis();

        return webClient.get()
            .uri(serverUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(3))
            .then(Mono.fromCallable(() -> (int) (System.currentTimeMillis() - startTime)))
            .onErrorResume(e -> {
                log.error("Failed to get response time for {}: {}", serverUrl, e.getMessage());
                return Mono.just(configProperties.getMaxResponseTime());
            });
    }
}
