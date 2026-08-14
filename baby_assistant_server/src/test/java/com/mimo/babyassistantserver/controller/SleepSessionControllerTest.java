package com.mimo.babyassistantserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.sleep.SleepSessionResponse;
import com.mimo.babyassistantserver.service.SleepSessionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SleepSessionController.class)
class SleepSessionControllerTest {
    private static final String BABY_ID = "618b988e-9588-4c64-9da8-b010c52e3f40";
    private static final String SESSION_ID = "5a307dc0-2e09-4d12-8eee-0f9d9574ee25";

    @Autowired private MockMvc mockMvc;
    @MockBean private SleepSessionService sleepSessionService;

    @Test
    void startsASleepSession() throws Exception {
        given(sleepSessionService.start(any())).willReturn(activeSession());

        mockMvc.perform(post("/api/v1/sleep-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"babyId\":\"" + BABY_ID + "\",\"startedAt\":\"2026-07-31T12:00:00Z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.endedAt").doesNotExist());
    }

    @Test
    void endsASleepSession() throws Exception {
        given(sleepSessionService.end(eq(UUID.fromString(SESSION_ID)), any())).willReturn(completedSession());

        mockMvc.perform(patch("/api/v1/sleep-sessions/{sessionId}/end", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endedAt\":\"2026-07-31T13:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endedAt").value("2026-07-31T13:00:00Z"));
    }

    @Test
    void listsSleepSessionsForADay() throws Exception {
        given(sleepSessionService.list(UUID.fromString(BABY_ID), null)).willReturn(List.of(completedSession()));

        mockMvc.perform(get("/api/v1/sleep-sessions").param("babyId", BABY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SESSION_ID));
    }

    @Test
    void deletesASleepSession() throws Exception {
        mockMvc.perform(delete("/api/v1/sleep-sessions/{sessionId}", SESSION_ID))
                .andExpect(status().isNoContent());

        verify(sleepSessionService).delete(UUID.fromString(SESSION_ID));
    }

    private static SleepSessionResponse activeSession() {
        return new SleepSessionResponse(
                UUID.fromString(SESSION_ID), UUID.fromString(BABY_ID),
                Instant.parse("2026-07-31T12:00:00Z"), null,
                Instant.parse("2026-07-31T12:00:00Z"));
    }

    private static SleepSessionResponse completedSession() {
        return new SleepSessionResponse(
                UUID.fromString(SESSION_ID), UUID.fromString(BABY_ID),
                Instant.parse("2026-07-31T12:00:00Z"), Instant.parse("2026-07-31T13:00:00Z"),
                Instant.parse("2026-07-31T12:00:00Z"));
    }
}