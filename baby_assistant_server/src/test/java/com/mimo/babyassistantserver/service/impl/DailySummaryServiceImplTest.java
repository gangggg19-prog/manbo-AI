package com.mimo.babyassistantserver.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.entity.BabyProfile;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.DailySummaryMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailySummaryServiceImplTest {
    @Mock private DailySummaryMapper dailySummaryMapper;
    @Mock private BabyProfileMapper babyProfileMapper;

    @Test
    void returnsTodayTotalsAndChangesFromYesterday() {
        UUID babyId = UUID.randomUUID();
        when(babyProfileMapper.selectById(babyId)).thenReturn(new BabyProfile());
        when(dailySummaryMapper.selectFeedingMl(eq(babyId), any(Instant.class), any(Instant.class)))
                .thenReturn(420, 360);
        when(dailySummaryMapper.selectDiaperCount(eq(babyId), any(Instant.class), any(Instant.class)))
                .thenReturn(5, 4);
        when(dailySummaryMapper.selectSleepMinutes(eq(babyId), any(Instant.class), any(Instant.class), any(Instant.class)))
                .thenReturn(510, 480);
        when(dailySummaryMapper.hasActiveSleepSession(babyId)).thenReturn(false);
        DailySummaryServiceImpl service = new DailySummaryServiceImpl(dailySummaryMapper, babyProfileMapper);

        DailySummaryResponse result = service.get(babyId, LocalDate.of(2026, 7, 31));

        assertEquals(420, result.feedingMl());
        assertEquals(5, result.diaperCount());
        assertEquals(510, result.sleepMinutes());
        assertEquals(60, result.feedingDeltaMl());
        assertEquals(1, result.diaperDelta());
        assertEquals(30, result.sleepDeltaMinutes());
        assertFalse(result.sleepInProgress());
        assertEquals("DAILY_RECORDS_READY", result.insight());
    }
}