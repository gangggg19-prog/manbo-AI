package com.mimo.babyassistantserver.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.service.DailySummaryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DailySummaryController.class)
class DailySummaryControllerTest {
    private static final String BABY_ID = "618b988e-9588-4c64-9da8-b010c52e3f40";

    @Autowired private MockMvc mockMvc;
    @MockBean private DailySummaryService dailySummaryService;

    @Test
    void returnsARealDataDailyBriefing() throws Exception {
        given(dailySummaryService.get(UUID.fromString(BABY_ID), LocalDate.of(2026, 7, 31)))
                .willReturn(new DailySummaryResponse(
                        LocalDate.of(2026, 7, 31), 420, 5, 510, 60, 1, 30, false, "DAILY_RECORDS_READY"));

        mockMvc.perform(get("/api/v1/daily-summary")
                        .param("babyId", BABY_ID)
                        .param("date", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedingMl").value(420))
                .andExpect(jsonPath("$.sleepMinutes").value(510))
                .andExpect(jsonPath("$.insight").value("DAILY_RECORDS_READY"));
    }
}