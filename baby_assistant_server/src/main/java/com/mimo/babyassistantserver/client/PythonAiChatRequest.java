package com.mimo.babyassistantserver.client;

import java.util.List;

/** Private HTTP contract sent from Java orchestration to the Python AI service. */
public record PythonAiChatRequest(
        int babyAgeMonths,
        String date,
        DailySummary dailySummary,
        List<HistoryMessage> history,
        List<KnowledgeSnippet> knowledge,
        String message) {
    public record DailySummary(
            int feedingMl,
            int diaperCount,
            int sleepMinutes,
            boolean sleepInProgress,
            String insight) {
    }

    /** Already ordered oldest-to-newest and bounded by Java before crossing services. */
    public record HistoryMessage(String role, String content) {
    }

    /** Reviewed context retrieved in Java and supplied to Python for grounding. */
    public record KnowledgeSnippet(String title, String content, String sourceName, String sourceUrl) {
    }
}
