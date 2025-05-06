package com.bervan.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OpenAIService implements AIService {
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    public static final String GPT_4 = "gpt-4";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String messageInitialPrompt;

    public OpenAIService(String messageInitialPrompt) {
        this.messageInitialPrompt = messageInitialPrompt;
    }

    public String askAI(String prompt, String model, double temperature, String apiKey) {
        if (Strings.isNullOrEmpty(apiKey)) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

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
}
