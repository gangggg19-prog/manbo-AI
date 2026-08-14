package com.mimo.babyassistantserver.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.sleep.EndSleepSessionRequest;
import com.mimo.babyassistantserver.dto.sleep.SleepSessionResponse;
import com.mimo.babyassistantserver.dto.sleep.StartSleepSessionRequest;

/** Business contract for starting, ending and reading sleep sessions. */
public interface SleepSessionService {
    SleepSessionResponse start(StartSleepSessionRequest request);
    SleepSessionResponse end(UUID sessionId, EndSleepSessionRequest request);
    List<SleepSessionResponse> list(UUID babyId, LocalDate date);
    void delete(UUID sessionId);
}