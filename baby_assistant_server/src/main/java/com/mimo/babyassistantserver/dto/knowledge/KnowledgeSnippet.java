package com.mimo.babyassistantserver.dto.knowledge;

/** Compact, traceable knowledge context passed to the Python AI service. */
public record KnowledgeSnippet(
        String title,
        String content,
        String sourceName,
        String sourceUrl) {
}