package com.mimo.babyassistantserver.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.dto.timeline.GrowthTimelineResponse;
import com.mimo.babyassistantserver.service.GrowthTimelineService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GrowthTimelineController.class)
class GrowthTimelineControllerTest {
    private static final String BABY_ID = "618b988e-9588-4c64-9da8-b010c52e3f40";

    @Autowired private MockMvc mockMvc;
    @MockBean private GrowthTimelineService growthTimelineService;

    @Test
    void returnsRecentDailySummaries() throws Exception {
        var point = new DailySummaryResponse(LocalDate.of(2026, 7, 31), 278, 1, 52, 278, 1, 52, true, "SLEEP_IN_PROGRESS");
        given(growthTimelineService.get(UUID.fromString(BABY_ID), 7))
                .willReturn(new GrowthTimelineResponse(LocalDate.of(2026, 7, 25), LocalDate.of(2026, 7, 31), List.of(point)));

        mockMvc.perform(get("/api/v1/growth-timeline").param("babyId", BABY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].feedingMl").value(278))
                .andExpect(jsonPath("$.days[0].insight").value("SLEEP_IN_PROGRESS"));
    }
}