package com.mimo.babyassistantserver.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.sleep.EndSleepSessionRequest;
import com.mimo.babyassistantserver.dto.sleep.SleepSessionResponse;
import com.mimo.babyassistantserver.dto.sleep.StartSleepSessionRequest;
import com.mimo.babyassistantserver.entity.SleepSession;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.SleepSessionMapper;
import com.mimo.babyassistantserver.service.SleepSessionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Enforces the sleep state machine: one active session per baby, then one valid end time.
 */
@Service
public class SleepSessionServiceImpl implements SleepSessionService {
    private final SleepSessionMapper sleepSessionMapper;
    private final BabyProfileMapper babyProfileMapper;

    public SleepSessionServiceImpl(
            SleepSessionMapper sleepSessionMapper,
            BabyProfileMapper babyProfileMapper) {
        this.sleepSessionMapper = sleepSessionMapper;
        this.babyProfileMapper = babyProfileMapper;
    }

    @Override
    @Transactional
    public SleepSessionResponse start(StartSleepSessionRequest request) {
        requireBaby(request.babyId());
        if (sleepSessionMapper.selectActiveByBabyId(request.babyId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A sleep session is already active");
        }
        SleepSession session = SleepSession.start(request.babyId(), request.startedAt());
        sleepSessionMapper.insert(session);
        return SleepSessionResponse.from(session);
    }

    @Override
    @Transactional
    public SleepSessionResponse end(UUID sessionId, EndSleepSessionRequest request) {
        SleepSession session = requireSession(sessionId);
        if (session.getEndedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The sleep session has already ended");
        }
        if (!request.endedAt().isAfter(session.getStartedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sleep end time must be after start time");
        }
        session.setEndedAt(request.endedAt());
        session.setUpdatedAt(Instant.now());
        if (sleepSessionMapper.finish(session) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The sleep session has already ended");
        }
        return SleepSessionResponse.from(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SleepSessionResponse> list(UUID babyId, LocalDate date) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        ZoneId zone = ZoneId.systemDefault();
        Instant start = selectedDate.atStartOfDay(zone).toInstant();
        Instant end = selectedDate.plusDays(1).atStartOfDay(zone).toInstant();
        return sleepSessionMapper.selectOverlappingDay(babyId, start, end)
                .stream().map(SleepSessionResponse::from).toList();
    }

    @Override
    @Transactional
    public void delete(UUID sessionId) {
        requireSession(sessionId);
        sleepSessionMapper.deleteById(sessionId);
    }

    private void requireBaby(UUID babyId) {
        if (babyProfileMapper.selectById(babyId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby profile was not found");
        }
    }

    private SleepSession requireSession(UUID sessionId) {
        SleepSession session = sleepSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleep session was not found");
        }
        return session;
    }
}