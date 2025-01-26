package com.example.acfac.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void logRequest(String clientIp, String requestUrl) {
        String message = String.format("{\"clientIp\": \"%s\", \"requestUrl\": \"%s\", \"timestamp\": %d}",
            clientIp, requestUrl, System.currentTimeMillis());
        kafkaTemplate.send("request", clientIp, message);
    }
}
