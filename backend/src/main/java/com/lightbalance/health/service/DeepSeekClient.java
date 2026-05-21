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
            return DeepSeekResult.unavailable("DeepSeek 集成已在配置中关闭。");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            return DeepSeekResult.unavailable("未检测到 DeepSeek API Key，请先配置 DEEPSEEK_API_KEY。");
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
                return DeepSeekResult.error("DeepSeek 接口返回异常（HTTP " + response.statusCode() + "）："
                    + abbreviate(response.body(), 220));
            }

            DeepSeekResponse parsed = objectMapper.readValue(response.body(), DeepSeekResponse.class);
            if (parsed.choices() == null || parsed.choices().isEmpty() || parsed.choices().get(0).message() == null) {
                return DeepSeekResult.error("DeepSeek 返回内容为空。");
            }

            String content = parsed.choices().get(0).message().content();
            if (!StringUtils.hasText(content)) {
                return DeepSeekResult.error("DeepSeek 返回了空白回答。");
            }

            return DeepSeekResult.success(content.trim(), properties.getModel());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return DeepSeekResult.error("DeepSeek 请求被中断：" + ex.getMessage());
        } catch (IOException ex) {
            return DeepSeekResult.error("DeepSeek 请求失败：" + ex.getMessage());
        }
    }

    public DeepSeekRuntime runtime() {
        boolean ready = properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
        String status = ready
            ? "DeepSeek 已连接"
            : "DeepSeek 未配置，请填写 DEEPSEEK_API_KEY 后重新部署。";
        return new DeepSeekRuntime("DeepSeek", properties.getModel(), ready, status);
    }

    private String abbreviate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "无返回内容";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
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
