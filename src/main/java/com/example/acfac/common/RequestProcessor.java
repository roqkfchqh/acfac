package com.example.acfac.common;

import com.example.acfac.kafka.KafkaRequestProducer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestProcessor {

    private final LoadBalancer loadBalancer;
    private final KafkaRequestProducer kafkaRequestProducer;

    /**
     * 클라이언트 요청 처리 -> 요청 정보 kafka 로깅
     * @param request 클라이언트 요청 데이터(JSON)
     * @param clientIp 클라이언트 ip (ipV6)
     * @return 서버 응답 데이터
     */
    public String processRequest(String request, String clientIp) {
        try {
            String serverUrl = loadBalancer.getNextServer();
            kafkaRequestProducer.logRequest(clientIp, serverUrl);
            URL url = new URL(serverUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(request.getBytes());
                os.flush();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (Exception e) {
            return "Error while processing request: " + e.getMessage();
        }
    }
}
