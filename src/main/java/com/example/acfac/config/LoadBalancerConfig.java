package com.example.acfac.config;

import com.example.acfac.common.HealthCheckService;
import com.example.acfac.common.LoadBalancerStrategy;
import com.example.acfac.concrete.Weighted;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoadBalancerConfig {

    @Bean
    @ConditionalOnProperty(name = "loadbalancer.strategy", havingValue = "weighted")
    public LoadBalancerStrategy weightedStrategy(HealthCheckService healthCheckService) {
        return new Weighted(healthCheckService);
    }
}
