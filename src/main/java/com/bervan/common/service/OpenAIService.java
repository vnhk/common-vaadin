package com.bervan.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OpenAIService implements AIService {
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    public static final String GPT_4 = "gpt-4";
    public static final String GPT_4O_MINI = "gpt-4o-mini";
    public static final String GPT_4O = "gpt-4o";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String messageInitialPrompt;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public OpenAIService(String messageInitialPrompt) {
        this.messageInitialPrompt = messageInitialPrompt;
    }

    public String askAI(String prompt, String model, double temperature, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        Map<String, Object> user = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> system = Map.of(
                "role", "system",
                "content", messageInitialPrompt +
                        ";do not any use previous messages as context; " +
                        "use only the current message as context");

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(system, user),
                "temperature", temperature
        );

        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("OpenAI API error: " + response.body());
            }

            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");

            return messageMap.get("content").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String askAIWithImage(String prompt, String base64Image, String mimeType, String model, double temperature, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        List<Map<String, Object>> userContent = List.of(
                Map.of("type", "text", "text", prompt),
                Map.of("type", "image_url", "image_url", Map.of("url", "data:" + mimeType + ";base64," + base64Image))
        );

        Map<String, Object> user = Map.of(
                "role", "user",
                "content", userContent
        );

        Map<String, Object> system = Map.of(
                "role", "system",
                "content", messageInitialPrompt +
                        ";do not any use previous messages as context; " +
                        "use only the current message as context");

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(system, user),
                "temperature", temperature
        );

        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("OpenAI API error: " + response.body());
            }

            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");

            return messageMap.get("content").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
