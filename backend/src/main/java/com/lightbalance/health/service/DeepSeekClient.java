package com.lightbalance.health.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbalance.health.config.DeepSeekProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DeepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
            .build();
    }

    public DeepSeekResult createChatCompletion(List<DeepSeekMessage> messages) {
        if (!properties.isEnabled()) {
            return DeepSeekResult.unavailable("DeepSeek integration is disabled in configuration.");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            return DeepSeekResult.unavailable("DeepSeek API key is missing. Set DEEPSEEK_API_KEY before starting the backend.");
        }

        try {
            DeepSeekRequest requestBody = new DeepSeekRequest(
                properties.getModel(),
                messages,
                new ThinkingConfig(properties.isThinkingEnabled() ? "enabled" : "disabled", properties.getReasoningEffort()),
                properties.getMaxTokens()
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return DeepSeekResult.error("DeepSeek API request failed with status " + response.statusCode() + ".");
            }

            DeepSeekResponse parsed = objectMapper.readValue(response.body(), DeepSeekResponse.class);
            if (parsed.choices() == null || parsed.choices().isEmpty() || parsed.choices().get(0).message() == null) {
                return DeepSeekResult.error("DeepSeek API returned an empty response.");
            }

            String content = parsed.choices().get(0).message().content();
            if (!StringUtils.hasText(content)) {
                return DeepSeekResult.error("DeepSeek API returned a blank answer.");
            }

            return DeepSeekResult.success(content.trim(), properties.getModel());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeepSeekResult.error("DeepSeek request failed: " + ex.getMessage());
        } catch (IOException ex) {
            return DeepSeekResult.error("DeepSeek request failed: " + ex.getMessage());
        }
    }

    public DeepSeekRuntime runtime() {
        boolean ready = properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
        String status = ready
            ? "DeepSeek connected"
            : "DeepSeek not configured. Set DEEPSEEK_API_KEY and restart the backend.";
        return new DeepSeekRuntime("DeepSeek", properties.getModel(), ready, status);
    }

    public record DeepSeekMessage(String role, String content) {
    }

    public record DeepSeekResult(boolean success, boolean configured, String content, String model, String errorMessage) {

        public static DeepSeekResult success(String content, String model) {
            return new DeepSeekResult(true, true, content, model, null);
        }

        public static DeepSeekResult unavailable(String message) {
            return new DeepSeekResult(false, false, null, null, message);
        }

        public static DeepSeekResult error(String message) {
            return new DeepSeekResult(false, true, null, null, message);
        }
    }

    public record DeepSeekRuntime(String provider, String model, boolean liveMode, String status) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record DeepSeekRequest(
        String model,
        List<DeepSeekMessage> messages,
        ThinkingConfig thinking,
        @JsonProperty("max_tokens") int maxTokens
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record ThinkingConfig(String type, @JsonProperty("reasoning_effort") String reasoningEffort) {
    }

    private record DeepSeekResponse(List<DeepSeekChoice> choices) {
    }

    private record DeepSeekChoice(DeepSeekChoiceMessage message) {
    }

    private record DeepSeekChoiceMessage(String content) {
    }
}
