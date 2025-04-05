package com.bervan.common.service;

public interface AIService {
    String askAI(String input, String model, double temperature, String apiKey);
}
