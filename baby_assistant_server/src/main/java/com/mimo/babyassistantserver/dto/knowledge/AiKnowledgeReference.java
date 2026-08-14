package com.mimo.babyassistantserver.dto.knowledge;

/** A source shown to parents alongside a grounded AI answer. */
public record AiKnowledgeReference(
        String title,
        String sourceName,
        String sourceUrl) {
}