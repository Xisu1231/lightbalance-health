package com.lightbalance.health.web;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "service", "lightbalance-health",
            "time", LocalDateTime.now(APP_ZONE).toString()
        );
    }
}
