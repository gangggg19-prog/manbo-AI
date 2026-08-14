package com.mimo.babyassistantserver.service;

import java.util.List;
import com.mimo.babyassistantserver.dto.knowledge.KnowledgeArticleResponse;

public interface KnowledgeArticleService {
    List<KnowledgeArticleResponse> list(Integer ageMonths, String category);
    KnowledgeArticleResponse getBySlug(String slug);
}