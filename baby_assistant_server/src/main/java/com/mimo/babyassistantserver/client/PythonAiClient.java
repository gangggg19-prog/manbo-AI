package com.mimo.babyassistantserver.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * The only Java class that knows where Python lives.  Moving Python behind
 * Nacos/OpenFeign later will therefore not leak into controllers or services.
 */
@Component
public class PythonAiClient {
    private final RestClient restClient;

    public PythonAiClient(@Value("${ai.service.base-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PythonAiChatResponse chat(PythonAiChatRequest request) {
        return restClient.post()
                .uri("/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PythonAiChatResponse.class);
    }
}
