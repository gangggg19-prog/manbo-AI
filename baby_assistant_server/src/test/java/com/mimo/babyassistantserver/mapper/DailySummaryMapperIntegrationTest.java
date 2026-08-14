package com.mimo.babyassistantserver.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DailySummaryMapperIntegrationTest {
    @Autowired private DailySummaryMapper dailySummaryMapper;

    @Test
    void aggregateQueriesReturnZeroForAnUnknownBaby() {
        UUID unknownBabyId = UUID.randomUUID();
        Instant start = Instant.parse("2026-07-31T00:00:00Z");
        Instant end = Instant.parse("2026-08-01T00:00:00Z");

        assertEquals(0, dailySummaryMapper.selectFeedingMl(unknownBabyId, start, end));
        assertEquals(0, dailySummaryMapper.selectDiaperCount(unknownBabyId, start, end));
        assertEquals(0, dailySummaryMapper.selectSleepMinutes(unknownBabyId, start, end, end));
        assertFalse(dailySummaryMapper.hasActiveSleepSession(unknownBabyId));
    }
}