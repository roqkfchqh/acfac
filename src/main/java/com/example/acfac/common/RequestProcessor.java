package com.example.acfac.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.stereotype.Component;

@Component
public class RequestProcessor {

    private final LoadBalancer loadBalancer;

    public RequestProcessor(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String processRequest(String request) {
        try {
            String serverUrl = loadBalancer.getNextServer();
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
