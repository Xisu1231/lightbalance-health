package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.AssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping
    public AppDtos.AssistantResponse getConversation() {
        return assistantService.getConversation();
    }

    @PostMapping("/message")
    public AppDtos.AssistantResponse sendMessage(@RequestBody AppDtos.AssistantRequest request) {
        return assistantService.reply(request.message());
    }
}
