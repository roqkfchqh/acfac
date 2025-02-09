package com.example.acfac.common;

import com.example.acfac.common.httpmethod.HttpMethodStrategy;
import com.example.acfac.kafka.KafkaRequestProducer;
import com.example.acfac.rest.UserRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestProcessor {

    private final LoadBalancer loadBalancer;
    private final KafkaRequestProducer kafkaRequestProducer;
    private final WebClient.Builder builder;
    private final HealthCheckService healthCheckService;

    /**
     * 클라이언트 요청 처리 (비동기 방식)
     *
     * @param dto 클라이언트 요청 데이터 (JSON 형식)
     * @param clientIp 클라이언트 IP
     * @return Mono<String> 비동기 서버 응답 데이터
     */
    public Mono<String> processRequest(UserRequestDto dto, String clientIp) {
        try {
            String serverUrl = loadBalancer.getNextServer(healthCheckService.getHealthyServers());

            Mono<Void> kafkaLogging = Mono.fromRunnable(() -> {
                try {
                    kafkaRequestProducer.logRequest(clientIp, serverUrl);
                } catch (Exception e) {
                    log.error("kafka 서버 오류: {}", e.getMessage());
                }
            });

            try {
                HttpMethodStrategy strategy = HttpMethodStrategy.valueOf(dto.httpMethod().toUpperCase());
                return kafkaLogging.then(
                    strategy.execute(builder, serverUrl, dto.json())
                );
            } catch (IllegalArgumentException e) {
                return Mono.error(new UnsupportedOperationException("지원하지 않는 HTTP METHOD: " + dto.httpMethod()));
            }

        } catch (Exception e) {
            return Mono.error(new RuntimeException(e.getMessage(), e));
        }
    }
}
