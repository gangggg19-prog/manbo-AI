package com.mimo.babyassistantserver.service;

import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;

/** Business contract for the home screen daily briefing. */
public interface DailySummaryService {
    DailySummaryResponse get(UUID babyId, LocalDate date);
}