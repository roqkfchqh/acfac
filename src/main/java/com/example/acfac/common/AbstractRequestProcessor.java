package com.example.acfac.common;

import com.example.acfac.rest.UserRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 템플릿 메서드 패턴을 적용한 추상 클래스
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRequestProcessor {

    protected final LoadBalancer loadBalancer;
    protected final WebClient webClient;
    protected final HealthCheckService healthCheckService;

    /**
     * 공통 처리 메서드 (템플릿 메서드)
     */
    public Mono<String> processRequest(UserRequestDto dto, String clientIp) {
        String serverUrl = loadBalancer.getNextServer(healthCheckService.getHealthyServers());

        // 하위 클래스에서 HTTP 요청 전략 실행
        return executeRequest(serverUrl, dto.json());
    }

    /**
     * 하위 클래스에서 HTTP 요청 실행 방식 구현
     */
    protected abstract Mono<String> executeRequest(String serverUrl, String requestBody);
}
