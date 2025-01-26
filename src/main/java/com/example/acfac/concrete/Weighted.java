package com.example.acfac.concrete;

import com.example.acfac.common.HealthCheckService;
import com.example.acfac.common.LoadBalancerStrategy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Weighted implements LoadBalancerStrategy {

    private final Map<String, Integer> serverWeights = new ConcurrentHashMap<>();
    private final HealthCheckService healthCheckService;
    private final ScheduledExecutorService scheduler;

    public Weighted(HealthCheckService healthCheckService, ScheduledExecutorService scheduler) {
        this.healthCheckService = healthCheckService;
        this.scheduler = scheduler;
        startWeightUpdater();
    }

    public Weighted(HealthCheckService healthCheckService) {
        this(healthCheckService, Executors.newScheduledThreadPool(1));
    }

    private void startWeightUpdater() {
        scheduler.scheduleAtFixedRate(() -> {
            List<String> healthyServers = healthCheckService.getHealthyServers();
            healthyServers.forEach(server -> {
                int responseTime = healthCheckService.getResponseTime(server);
                updateWeight(server, responseTime);
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void updateWeight(String server, int responseTime) {
        int newWeight = Math.max(1, 10 - (responseTime / 100));
        serverWeights.put(server, newWeight);
    }

    @Override
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

    public void stopWeightUpdater() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
