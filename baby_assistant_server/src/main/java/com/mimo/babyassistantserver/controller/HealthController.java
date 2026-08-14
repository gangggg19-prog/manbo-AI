package com.mimo.babyassistantserver.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务存活检查接口。
 * 用于本地联调、部署探针和网关健康检查，不承载业务数据。
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "baby-assistant-server", "status", "ok", "time", Instant.now().toString());
    }
}