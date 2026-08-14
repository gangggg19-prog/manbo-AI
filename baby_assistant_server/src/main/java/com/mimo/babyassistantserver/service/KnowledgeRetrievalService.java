package com.mimo.babyassistantserver.service;

import java.util.List;

import com.mimo.babyassistantserver.dto.knowledge.KnowledgeSnippet;

/** Retrieves only age-appropriate, relevant knowledge before a model call. */
public interface KnowledgeRetrievalService {
    List<KnowledgeSnippet> retrieve(String question, int ageMonths);
}