package com.bervan.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BaseScanningService {
    private static final Logger log = LoggerFactory.getLogger(BaseScanningService.class);
    private final OpenAIService openAIService;
    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final String apiKey;
    private String mimeType = "image/jpeg";


    public BaseScanningService(String messageInitialPrompt, String apiKey) {
        this.openAIService = new OpenAIService(messageInitialPrompt);
        this.apiKey = apiKey;
    }

    private static String compressImage(byte[] imageBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(900, 900)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .keepAspectRatio(true)
                .toOutputStream(baos);

        byte[] compressedBytes = baos.toByteArray();
        String compressedBase64 = Base64.getEncoder().encodeToString(compressedBytes);
        return compressedBase64;
    }

    private String getCleanedResponse(String response) {
        String cleanedResponse = response.trim();
        if (cleanedResponse.startsWith("```")) {
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            } else {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();
        }
        return cleanedResponse;
    }

    public String askAIWithImage(String base64Image, String prompt) throws IOException {

        byte[] imageBytes = convertToBytes(base64Image, base64Image);
        String compressedBase64 = compressImage(imageBytes);

        log.info("Sending image to OpenAI for analysis...");
        String response = openAIService.askAIWithImage(prompt, compressedBase64, mimeType, OpenAIService.GPT_4O_MINI, 0.1, apiKey);

        if (response == null) {
            log.error("Failed to get response from OpenAI or API key is not configured.");
            return null;
        }

        log.info("OpenAI response successfully received.");

        // Clean response if markdown code block was returned despite instructions
        return getCleanedResponse(response);
    }

    private byte[] convertToBytes(String base64Image, String base64Data) {
        if (base64Image.startsWith("data:")) {
            int commaIndex = base64Image.indexOf(",");
            if (commaIndex != -1) {
                String prefix = base64Image.substring(0, commaIndex);
                if (prefix.contains(":") && prefix.contains(";")) {
                    mimeType = prefix.substring(prefix.indexOf(":") + 1, prefix.indexOf(";"));
                }
                base64Image = base64Image.substring(commaIndex + 1);
            }
        }

        return Base64.getDecoder().decode(base64Image);
    }
}
