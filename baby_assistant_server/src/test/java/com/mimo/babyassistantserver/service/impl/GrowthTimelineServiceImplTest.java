package com.mimo.babyassistantserver.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.service.DailySummaryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrowthTimelineServiceImplTest {
    @Mock private DailySummaryService dailySummaryService;

    @Test
    void returnsOneOrderedSummaryForEachRequestedDay() {
        UUID babyId = UUID.randomUUID();
        when(dailySummaryService.get(eq(babyId), org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    return new DailySummaryResponse(date, 0, 0, 0, 0, 0, 0, false, "START_RECORDING");
                });
        GrowthTimelineServiceImpl service = new GrowthTimelineServiceImpl(dailySummaryService);

        var result = service.get(babyId, 7);

        assertEquals(7, result.days().size());
        assertEquals(result.startDate(), result.days().get(0).date());
        assertEquals(result.endDate(), result.days().get(result.days().size() - 1).date());
    }
}