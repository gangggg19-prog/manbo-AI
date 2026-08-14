package com.mimo.babyassistantserver.dto.timeline;

import java.time.LocalDate;
import java.util.List;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;

/** Ordered set of daily summaries used by the growth timeline. */
public record GrowthTimelineResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<DailySummaryResponse> days) {
}