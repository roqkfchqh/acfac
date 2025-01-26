package com.example.acfac.common;

import com.example.acfac.values.LoadBalancerConfigProperties;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckService {

    private final LoadBalancerConfigProperties configProperties;
    private final ConcurrentMap<String, Boolean> healthyServers = new ConcurrentHashMap<>();

    public List<String> getHealthyServers() {
        return healthyServers.entrySet().stream()
            .filter(Entry::getValue)
            .map(Entry::getKey)
            .toList();
    }

    @Scheduled(fixedRateString = "${healthcheck.fixedRate}")
    public void performHealthCheck() {
        List<String> allServers = configProperties.getServers();

        allServers.forEach(server -> {
            boolean isHealthy = isServerHealthy(server);
            healthyServers.put(server, isHealthy);
        });

        log.info("Updated healthy servers: {}", getHealthyServers());
    }

    public int getResponseTime(String serverUrl) {
        try {
            HttpURLConnection connection = createHttpConnection(serverUrl + "/actuator/health");
            long startTime = System.currentTimeMillis();
            connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            return (int) (endTime - startTime);
        } catch (Exception e) {
            log.error("Failed to get response time for {}: {}", serverUrl, e.getMessage());
            return configProperties.getMaxResponseTime();
        }
    }

    private boolean isServerHealthy(String serverUrl) {
        try {
            HttpURLConnection connection = createHttpConnection(serverUrl + "/actuator/health");
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            log.error("Health check failed for {}: {}", serverUrl, e.getMessage());
            return false;
        }
    }

    private HttpURLConnection createHttpConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(configProperties.getMaxResponseTime());
        connection.setReadTimeout(configProperties.getMaxResponseTime());
        return connection;
    }
}
