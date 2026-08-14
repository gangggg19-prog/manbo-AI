package com.mimo.babyassistantserver.dto.knowledge;

import java.util.UUID;

/** Public resource-card payload; content is provided only on article detail. */
public record KnowledgeArticleResponse(
        UUID id, String slug, String title, String category,
        int minAgeMonths, Integer maxAgeMonths, String content,
        String sourceName, String sourceUrl) { }