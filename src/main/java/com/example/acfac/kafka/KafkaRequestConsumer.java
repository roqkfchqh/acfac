package com.example.acfac.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaRequestConsumer {

    @KafkaListener(topics = "request", groupId = "acfac-group")
    public void consumeMessage(String message) {
        log.info("Consumed message: {}", message);
    }
}
