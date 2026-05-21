package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.AssistantService;
import com.lightbalance.health.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final AuthService authService;

    public AssistantController(AssistantService assistantService, AuthService authService) {
        this.assistantService = assistantService;
        this.authService = authService;
    }

    @GetMapping
    public AppDtos.AssistantResponse getConversation(HttpServletRequest request) {
        return assistantService.getConversation(authService.requireCurrentUser(request));
    }

    @PostMapping("/message")
    public AppDtos.AssistantResponse sendMessage(
        HttpServletRequest servletRequest,
        @RequestBody AppDtos.AssistantRequest request
    ) {
        return assistantService.reply(authService.requireCurrentUser(servletRequest), request.message());
    }

    @PostMapping("/trend-advice")
    public AppDtos.TrendAdviceResponse trendAdvice(HttpServletRequest request) {
        return assistantService.trendAdvice(authService.requireCurrentUser(request));
    }
}
