package com.example.acfac.kafka;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

@Component
public class KafkaRequestProducer {

    private final KafkaProducer<String, String> producer;
    private static final String TOPIC = "request";

    public KafkaRequestProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producer = new KafkaProducer<>(props);
    }

    public void logRequest(String clientIp, String requestUrl){
        //요청데이터 JSON 형식으로 재구성
        String message = String.format("{\"clientIp\": \"%s\", \"requestUrl\": \"%s\", \"timestamp\": %d}",
            clientIp, requestUrl, System.currentTimeMillis());
        producer.send(new ProducerRecord<>(TOPIC, clientIp, message));
    }

    public void close() {
        producer.close();
    }
}
