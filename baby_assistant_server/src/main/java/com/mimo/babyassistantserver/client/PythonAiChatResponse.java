package com.mimo.babyassistantserver.client;

import java.util.List;

/** Response returned by the isolated Python AI service. */
public record PythonAiChatResponse(
        String reply,
        String safetyNotice,
        String source,
        List<String> suggestedActions,
        List<KnowledgeReference> references) {
    public record KnowledgeReference(String title, String sourceName, String sourceUrl) {
    }
}