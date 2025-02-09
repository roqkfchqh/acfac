package com.example.acfac.common;

import com.example.acfac.values.LoadBalancerConfigProperties;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoadBalancer {

    private final Map<String, Integer> serverWeights = new ConcurrentHashMap<>();
    private final Map<String, Integer> initialWeights = new ConcurrentHashMap<>();
    private final HealthCheckService healthCheckService;
    private final ScheduledExecutorService scheduler;
    private final LoadBalancerConfigProperties configProperties;

    public LoadBalancer(HealthCheckService healthCheckService, LoadBalancerConfigProperties configProperties) {
        this.healthCheckService = healthCheckService;
        this.configProperties = configProperties;
        this.scheduler = Executors.newScheduledThreadPool(1);
        initialWeights(configProperties);
        startWeightUpdater();
    }

    private void initialWeights(LoadBalancerConfigProperties config) {
        List<String> servers = config.getServers();
        List<Integer> weights = config.getWeights();
        for (int i = 0; i < servers.size(); i++) {
            initialWeights.put(servers.get(i), weights.get(i));
        }
    }

    private void startWeightUpdater() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<String> healthyServers = healthCheckService.getHealthyServers();
                if (healthyServers.isEmpty()) {
                    log.warn("No healthy servers found.");
                    return;
                }
                healthyServers.forEach(server -> {
                    int responseTime = healthCheckService.getResponseTime(server);
                    updateWeight(server, responseTime);
                });
            } catch (Exception e) {
                log.error("Error during weight update: {}", e.getMessage());
            }
        }, 0, configProperties.getWeightUpdatePeriod(), TimeUnit.SECONDS);
    }

    private void updateWeight(String server, int responseTime) {
        int baseWeight = initialWeights.getOrDefault(server, 1);
        int newWeight = Math.max(1, baseWeight * (10 - responseTime / 100));
        serverWeights.put(server, newWeight);
        // TODO 가중치 설정방식 조정필요
    }

    public String getNextServer(List<String> healthyServers) {
        int totalWeight = serverWeights.entrySet().stream()
            .filter(entry -> healthyServers.contains(entry.getKey()))
            .mapToInt(Map.Entry::getValue)
            .sum();
        int random = (int) (Math.random() * totalWeight);

        for (Map.Entry<String, Integer> entry : serverWeights.entrySet()) {
            if (!healthyServers.contains(entry.getKey())) continue;
            random -= entry.getValue();
            if (random < 0) return entry.getKey();
        }
        throw new IllegalStateException("No server found");
    }

    @PreDestroy
    public void shutdown() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
