package com.mimo.babyassistantserver.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.dto.timeline.GrowthTimelineResponse;
import com.mimo.babyassistantserver.service.DailySummaryService;
import com.mimo.babyassistantserver.service.GrowthTimelineService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reuses the daily-summary business rules so every screen uses identical totals. */
@Service
public class GrowthTimelineServiceImpl implements GrowthTimelineService {
    private final DailySummaryService dailySummaryService;

    public GrowthTimelineServiceImpl(DailySummaryService dailySummaryService) {
        this.dailySummaryService = dailySummaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public GrowthTimelineResponse get(UUID babyId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        List<DailySummaryResponse> summaries = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            summaries.add(dailySummaryService.get(babyId, date));
        }
        return new GrowthTimelineResponse(startDate, endDate, summaries);
    }
}