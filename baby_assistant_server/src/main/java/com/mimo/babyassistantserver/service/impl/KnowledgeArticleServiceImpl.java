package com.mimo.babyassistantserver.service.impl;

import java.util.List;
import com.mimo.babyassistantserver.dto.knowledge.KnowledgeArticleResponse;
import com.mimo.babyassistantserver.entity.KnowledgeArticle;
import com.mimo.babyassistantserver.mapper.KnowledgeArticleMapper;
import com.mimo.babyassistantserver.service.KnowledgeArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Resource centre reads the same reviewed articles used by RAG. */
@Service
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {
    private final KnowledgeArticleMapper mapper;
    public KnowledgeArticleServiceImpl(KnowledgeArticleMapper mapper) { this.mapper = mapper; }
    @Override public List<KnowledgeArticleResponse> list(Integer ageMonths, String category) {
        int age = ageMonths == null ? 0 : Math.max(0, ageMonths);
        return mapper.selectForAge(age).stream()
                .filter(article -> category == null || category.isBlank() || article.getCategory().equalsIgnoreCase(category))
                .map(this::toResponse).toList();
    }
    @Override public KnowledgeArticleResponse getBySlug(String slug) {
        KnowledgeArticle article = mapper.selectBySlug(slug);
        if (article == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Knowledge article was not found");
        return toResponse(article);
    }
    private KnowledgeArticleResponse toResponse(KnowledgeArticle a) {
        return new KnowledgeArticleResponse(a.getId(), a.getSlug(), a.getTitle(), a.getCategory(), a.getMinAgeMonths(), a.getMaxAgeMonths(), a.getContent(), a.getSourceName(), a.getSourceUrl());
    }
}