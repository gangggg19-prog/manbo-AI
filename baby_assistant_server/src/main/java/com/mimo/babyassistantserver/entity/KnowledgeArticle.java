package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/** A reviewed knowledge article used as grounded context for the AI assistant. */
public class KnowledgeArticle {
    private UUID id;
    private String slug;
    private String title;
    private String category;
    private int minAgeMonths;
    private Integer maxAgeMonths;
    private String keywords;
    private String content;
    private String sourceName;
    private String sourceUrl;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getMinAgeMonths() { return minAgeMonths; }
    public void setMinAgeMonths(int minAgeMonths) { this.minAgeMonths = minAgeMonths; }
    public Integer getMaxAgeMonths() { return maxAgeMonths; }
    public void setMaxAgeMonths(Integer maxAgeMonths) { this.maxAgeMonths = maxAgeMonths; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}