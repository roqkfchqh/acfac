package com.example.acfac.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadBalancer {

    private final LoadBalancerStrategy strategy;
    private final HealthCheckService healthCheckService;

    public String getNextServer(){
        return strategy.getNextServer(healthCheckService.getHealthyServers());
    }
}
