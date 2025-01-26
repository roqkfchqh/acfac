package com.example.acfac.common;

import com.example.acfac.values.LoadBalancerConfigProperties;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckService {

    private final LoadBalancerConfigProperties configProperties;
    private final CopyOnWriteArrayList<String> healthyServers = new CopyOnWriteArrayList<>();

    public List<String> getHealthyServers() {
        return List.copyOf(healthyServers);
    }

    @Scheduled(fixedRateString = "${healthcheck.fixedRate}")
    public void performHealthCheck() {
        List<String> allServers = new ArrayList<>(configProperties.servers());

        allServers.forEach(server -> {
            if (isServerHealthy(server)) {
                if (!healthyServers.contains(server)) {
                    healthyServers.add(server);
                }
            } else {
                healthyServers.remove(server);
            }
        });

        log.info("Updated healthy servers: {}", healthyServers);
    }

    public int getResponseTime(String serverUrl) {
        try {
            long startTime = System.currentTimeMillis();
            URL url = new URL(serverUrl + "/actuator/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(configProperties.maxResponseTime());
            connection.setReadTimeout(configProperties.maxResponseTime());
            connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            return (int) (endTime - startTime);
        } catch (Exception e) {
            log.error("Failed to get response time for {}: {}", serverUrl, e.getMessage());
            return configProperties.maxResponseTime();
        }
    }

    private boolean isServerHealthy(String serverUrl) {
        try {
            URL url = new URL(serverUrl + "/actuator/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(configProperties.maxResponseTime());
            connection.setReadTimeout(configProperties.maxResponseTime());
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            log.error("Health check failed for {}: {}", serverUrl, e.getMessage());
            return false;
        }
    }
}