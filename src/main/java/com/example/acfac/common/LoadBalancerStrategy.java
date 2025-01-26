package com.example.acfac.common;

import java.util.List;

public interface LoadBalancerStrategy {

    String getNextServer(List<String> healthyServers);
}
