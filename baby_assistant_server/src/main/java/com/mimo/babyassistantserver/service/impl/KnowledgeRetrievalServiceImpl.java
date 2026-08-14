package com.mimo.babyassistantserver.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mimo.babyassistantserver.dto.knowledge.KnowledgeSnippet;
import com.mimo.babyassistantserver.entity.KnowledgeArticle;
import com.mimo.babyassistantserver.mapper.KnowledgeArticleMapper;
import com.mimo.babyassistantserver.service.KnowledgeRetrievalService;

import org.springframework.stereotype.Service;

/**
 * First-stage RAG retrieval: age filtering in SQL and transparent keyword ranking in Java.
 * Elasticsearch/vector retrieval can replace this implementation without changing AI callers.
 */
@Service
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {
    private static final int MAX_SNIPPETS = 3;
    private static final Pattern NUMERIC_MONTH = Pattern.compile("(?<!\\d)(\\d{1,2})\\s*(?:个)?月");
    private final KnowledgeArticleMapper knowledgeArticleMapper;

    public KnowledgeRetrievalServiceImpl(KnowledgeArticleMapper knowledgeArticleMapper) {
        this.knowledgeArticleMapper = knowledgeArticleMapper;
    }

    @Override
    public List<KnowledgeSnippet> retrieve(String question, int ageMonths) {
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        return knowledgeArticleMapper.selectForAge(Math.max(0, ageMonths)).stream()
                .map(article -> new RankedArticle(article, score(article, normalizedQuestion)))
                .filter(ranked -> ranked.score() > 0)
                .sorted(Comparator.comparingInt(RankedArticle::score).reversed())
                .limit(MAX_SNIPPETS)
                .map(ranked -> toSnippet(ranked.article()))
                .toList();
    }


    /** A parent may ask a hypothetical age question; that explicit age beats profile age for retrieval. */
    private int mentionedAgeMonths(String question, int profileAgeMonths) {
        Matcher numericMatch = NUMERIC_MONTH.matcher(question);
        if (numericMatch.find()) {
            return Math.max(0, Integer.parseInt(numericMatch.group(1)));
        }
        String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        for (int index = 0; index < chineseNumbers.length; index++) {
            if (question.contains(chineseNumbers[index] + "个月") || question.contains(chineseNumbers[index] + "月龄")) {
                return index + 1;
            }
        }
        return Math.max(0, profileAgeMonths);
    }
    private int score(KnowledgeArticle article, String question) {
        int score = 0;
        for (String rawKeyword : article.getKeywords().split(",")) {
            String keyword = rawKeyword.trim().toLowerCase(Locale.ROOT);
            if (keyword.length() >= 2 && question.contains(keyword)) {
                score += 4;
            }
        }
        if (question.contains(article.getTitle().toLowerCase(Locale.ROOT))) {
            score += 6;
        }
        return score;
    }

    private KnowledgeSnippet toSnippet(KnowledgeArticle article) {
        return new KnowledgeSnippet(
                article.getTitle(), article.getContent(), article.getSourceName(), article.getSourceUrl());
    }

    private record RankedArticle(KnowledgeArticle article, int score) {
    }
}