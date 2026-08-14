package com.mimo.babyassistantserver.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.DailySummaryMapper;
import com.mimo.babyassistantserver.service.DailySummaryService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Combines raw care and sleep records into one read-only daily view. */
@Service
public class DailySummaryServiceImpl implements DailySummaryService {
    private final DailySummaryMapper dailySummaryMapper;
    private final BabyProfileMapper babyProfileMapper;

    public DailySummaryServiceImpl(
            DailySummaryMapper dailySummaryMapper,
            BabyProfileMapper babyProfileMapper) {
        this.dailySummaryMapper = dailySummaryMapper;
        this.babyProfileMapper = babyProfileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DailySummaryResponse get(UUID babyId, LocalDate date) {
        requireBaby(babyId);
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        DayRange today = DayRange.of(selectedDate);
        DayRange yesterday = DayRange.of(selectedDate.minusDays(1));
        Instant now = Instant.now();

        int feedingMl = dailySummaryMapper.selectFeedingMl(babyId, today.start(), today.end());
        int diaperCount = dailySummaryMapper.selectDiaperCount(babyId, today.start(), today.end());
        int sleepMinutes = dailySummaryMapper.selectSleepMinutes(babyId, today.start(), today.end(), now);
        int previousFeedingMl = dailySummaryMapper.selectFeedingMl(babyId, yesterday.start(), yesterday.end());
        int previousDiaperCount = dailySummaryMapper.selectDiaperCount(babyId, yesterday.start(), yesterday.end());
        int previousSleepMinutes = dailySummaryMapper.selectSleepMinutes(babyId, yesterday.start(), yesterday.end(), now);
        boolean sleepInProgress = dailySummaryMapper.hasActiveSleepSession(babyId);

        return new DailySummaryResponse(
                selectedDate,
                feedingMl,
                diaperCount,
                sleepMinutes,
                feedingMl - previousFeedingMl,
                diaperCount - previousDiaperCount,
                sleepMinutes - previousSleepMinutes,
                sleepInProgress,
                insight(feedingMl, diaperCount, sleepMinutes, sleepInProgress));
    }

    private void requireBaby(UUID babyId) {
        if (babyProfileMapper.selectById(babyId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby profile was not found");
        }
    }

    private String insight(int feedingMl, int diaperCount, int sleepMinutes, boolean sleepInProgress) {
        if (sleepInProgress) return "SLEEP_IN_PROGRESS";
        if (feedingMl == 0 && diaperCount == 0 && sleepMinutes == 0) return "START_RECORDING";
        if (feedingMl == 0) return "ADD_FEEDING";
        if (sleepMinutes == 0) return "ADD_SLEEP";
        return "DAILY_RECORDS_READY";
    }

    private record DayRange(Instant start, Instant end) {
        static DayRange of(LocalDate date) {
            ZoneId zone = ZoneId.systemDefault();
            return new DayRange(
                    date.atStartOfDay(zone).toInstant(),
                    date.plusDays(1).atStartOfDay(zone).toInstant());
        }
    }
}