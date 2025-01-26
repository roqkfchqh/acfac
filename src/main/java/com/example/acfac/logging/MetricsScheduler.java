package com.example.acfac.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsScheduler {

    private final MetricsRecorder metricsRecorder;

    @Scheduled(fixedRate = 1000)
    public void logTPS() {
        metricsRecorder.logMetrics();
        metricsRecorder.resetMetrics();
    }
}