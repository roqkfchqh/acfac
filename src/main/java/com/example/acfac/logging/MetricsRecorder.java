package com.example.acfac.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetricsRecorder {

    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger errorRequests = new AtomicInteger(0);

    public void incrementRequest() {
        totalRequests.incrementAndGet();
    }

    public void incrementError() {
        errorRequests.incrementAndGet();
    }

    public void logMetrics() {
        int total = totalRequests.get();
        int errors = errorRequests.get();
        double errorRate = total > 0 ? (errors / (double) total) * 100 : 0;

        String logMessage = String.format("Metrics - Total Requests: %d, Errors: %d, Error Rate: %.2f%%",
                total, errors, errorRate);
        log.info(logMessage);
        logMetricsToFile(logMessage);
    }

    public void resetMetrics() {
        totalRequests.set(0);
        errorRequests.set(0);
    }

    public void logMetricsToFile(String logMessage) {
        try (FileWriter writer = new FileWriter("metrics.log", true)) {
            writer.write(LocalDateTime.now() + " - " + logMessage + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
