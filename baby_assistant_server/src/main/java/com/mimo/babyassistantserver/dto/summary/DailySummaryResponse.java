package com.mimo.babyassistantserver.dto.summary;

import java.time.LocalDate;

/** Immutable daily totals returned to the mobile home screen. */
public record DailySummaryResponse(
        LocalDate date,
        int feedingMl,
        int diaperCount,
        int sleepMinutes,
        int feedingDeltaMl,
        int diaperDelta,
        int sleepDeltaMinutes,
        boolean sleepInProgress,
        String insight) {
}