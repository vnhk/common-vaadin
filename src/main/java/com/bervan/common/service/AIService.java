package com.bervan.common.service;

public interface AIService {
    String askAI(String input, String model, double temperature, String apiKey);
    String askAIWithImage(String prompt, String base64Image, String mimeType, String model, double temperature, String apiKey);
}
