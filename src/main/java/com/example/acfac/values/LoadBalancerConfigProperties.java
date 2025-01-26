package com.example.acfac.values;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record LoadBalancerConfigProperties(
    @Value("${servers}") List<String> servers,
    @Value("${weights}") List<Integer> weights,
    @Value("${max.response.time}") int maxResponseTime
) {}
