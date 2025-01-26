package com.example.acfac.concrete;

import com.example.acfac.common.HealthCheckService;
import com.example.acfac.common.LoadBalancerStrategy;
import com.example.acfac.values.LoadBalancerConfigProperties;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Weighted implements LoadBalancerStrategy {

    //동적 가중치
    private final Map<String, Integer> serverWeights = new ConcurrentHashMap<>();
    //초기 가중치
    private final Map<String, Integer> initialWeights = new ConcurrentHashMap<>();
    private final HealthCheckService healthCheckService;
    private final ScheduledExecutorService scheduler;

    /**
     * 가중치 업데이트는 동시성 문제를 방지하기 위해 단일스레드에서만 실행
     * @param healthCheckService 서버 상태 확인 서비스
     * @param config 서버 URL & 초기가중치 설정값
     */
    public Weighted(HealthCheckService healthCheckService, LoadBalancerConfigProperties config) {
        this.healthCheckService = healthCheckService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        initialWeights(config);
        startWeightUpdater();
    }

    private void initialWeights(LoadBalancerConfigProperties config) {
        List<String> servers = config.servers();
        List<Integer> weights = config.weights();
        for(int i = 0; i < servers.size(); i++){
            initialWeights.put(servers.get(i), weights.get(i));
        }
    }

    //정상 서버를 가져와 응답 시간을 기반으로 동적 가중치 계산
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
        int baseWeight = initialWeights.getOrDefault(server, 1);
        int newWeight = Math.max(1, baseWeight * (10 - responseTime / 100));
        serverWeights.put(server, newWeight);
    }

    /**
     * 요청을 처리할 서버를 가중치 기반으로 선택
     * 총 가중치 기반으로 랜덤값 생성 -> 각 서버 가중치 차감 -> 적합한 서버 선택.
     * @param healthyServers 정상 서버 리스트
     * @return 선택된 서버 URL
     * @throws IllegalStateException 사용가능한 서버 없는 경우
     */
    @Override
    public String getNextServer(List<String> healthyServers) {
        int totalWeight = serverWeights.entrySet().stream()
            .filter(entry -> healthyServers.contains(entry.getKey()))
            .mapToInt(Map.Entry::getValue)
            .sum();
        int random = (int) (Math.random() * totalWeight);

        for (Map.Entry<String, Integer> entry : serverWeights.entrySet()) {
            if (!healthyServers.contains(entry.getKey())) continue; //비정상 서버 제외
            random -= entry.getValue();
            if (random < 0) return entry.getKey();
        }
        throw new IllegalStateException("No server found");
    }

    //가중치 업데이트 종료
    public void stopWeightUpdater() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
