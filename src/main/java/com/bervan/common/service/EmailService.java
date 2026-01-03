package com.bervan.common.service;

import com.bervan.logging.JsonLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");

    @Value("${email-service-api-key}")
    private String API_KEY;
    @Value("${email-service-api-url}")
    private String API_URL;
    @Value("${email-service-email-sender}")
    private String SENDER_EMAIL;

    public void sendEmail(String to, String subject, String htmlContent, String emailTitle) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", API_KEY);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = Map.of(
                "name", emailTitle,
                "email", SENDER_EMAIL
        );

        Map<String, String> toRecipient = Map.of(
                "email", to,
                "name", "User"
        );

        body.put("sender", sender);
        body.put("to", List.of(toRecipient));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            System.out.println("Email sent! Response: " + response.getBody());
        } catch (Exception e) {
            log.error("Email sending failed: " + e.getMessage());
        }
    }
}