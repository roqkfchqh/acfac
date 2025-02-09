package com.example.acfac.values;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerConfigProperties {

    private List<String> servers;
    private List<Integer> weights;
    private int maxResponseTime;
    private int weightUpdatePeriod;

    @PostConstruct
    public void validate() {
        if (servers == null || weights == null || servers.size() != weights.size()) {
            throw new IllegalArgumentException("servers와 weights의 크기가 일치하지 않습니다.");
        }
        if (weightUpdatePeriod <= 0) {
            throw new IllegalArgumentException("weightUpdatePeriod는 0보다 커야 합니다.");
        }
    }
}
