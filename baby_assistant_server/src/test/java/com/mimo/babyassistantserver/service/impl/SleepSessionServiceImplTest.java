package com.mimo.babyassistantserver.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.sleep.EndSleepSessionRequest;
import com.mimo.babyassistantserver.dto.sleep.StartSleepSessionRequest;
import com.mimo.babyassistantserver.entity.SleepSession;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.SleepSessionMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SleepSessionServiceImplTest {
    @Mock private SleepSessionMapper sleepSessionMapper;
    @Mock private BabyProfileMapper babyProfileMapper;

    @Test
    void rejectsStartingAnotherActiveSession() {
        UUID babyId = UUID.randomUUID();
        when(babyProfileMapper.selectById(babyId)).thenReturn(new com.mimo.babyassistantserver.entity.BabyProfile());
        when(sleepSessionMapper.selectActiveByBabyId(babyId)).thenReturn(
                SleepSession.start(babyId, Instant.parse("2026-07-31T12:00:00Z")));
        SleepSessionServiceImpl service = new SleepSessionServiceImpl(sleepSessionMapper, babyProfileMapper);

        assertThrows(ResponseStatusException.class, () -> service.start(
                new StartSleepSessionRequest(babyId, Instant.parse("2026-07-31T13:00:00Z"))));
    }

    @Test
    void rejectsEndingBeforeTheSessionStarted() {
        UUID sessionId = UUID.randomUUID();
        SleepSession active = SleepSession.start(UUID.randomUUID(), Instant.parse("2026-07-31T12:00:00Z"));
        when(sleepSessionMapper.selectById(sessionId)).thenReturn(active);
        SleepSessionServiceImpl service = new SleepSessionServiceImpl(sleepSessionMapper, babyProfileMapper);

        assertThrows(ResponseStatusException.class, () -> service.end(
                sessionId, new EndSleepSessionRequest(Instant.parse("2026-07-31T11:59:00Z"))));
    }
}