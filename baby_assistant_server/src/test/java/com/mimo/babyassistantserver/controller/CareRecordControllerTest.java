package com.mimo.babyassistantserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.care.CareRecordResponse;
import com.mimo.babyassistantserver.entity.CareRecordType;
import com.mimo.babyassistantserver.service.CareRecordService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CareRecordController.class)
class CareRecordControllerTest {
    private static final String BABY_ID = "618b988e-9588-4c64-9da8-b010c52e3f40";
    private static final String RECORD_ID = "8a307dc0-2e09-4d12-8eee-0f9d9574ee25";

    @Autowired private MockMvc mockMvc;
    @MockBean private CareRecordService careRecordService;

    @Test
    void createsAFeedingRecord() throws Exception {
        given(careRecordService.create(any())).willReturn(response(CareRecordType.FEEDING, 120));

        mockMvc.perform(post("/api/v1/care-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("FEEDING", 120)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountMl").value(120));
    }

    @Test
    void listsCareRecordsForABaby() throws Exception {
        given(careRecordService.list(UUID.fromString(BABY_ID), null))
                .willReturn(List.of(response(CareRecordType.DIAPER, null)));

        mockMvc.perform(get("/api/v1/care-records").param("babyId", BABY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DIAPER"));
    }

    @Test
    void updatesARecord() throws Exception {
        given(careRecordService.update(eq(UUID.fromString(RECORD_ID)), any()))
                .willReturn(response(CareRecordType.FEEDING, 150));

        mockMvc.perform(put("/api/v1/care-records/{recordId}", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("FEEDING", 150)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountMl").value(150));
    }

    @Test
    void deletesARecord() throws Exception {
        mockMvc.perform(delete("/api/v1/care-records/{recordId}", RECORD_ID))
                .andExpect(status().isNoContent());

        verify(careRecordService).delete(UUID.fromString(RECORD_ID));
    }

    private static CareRecordResponse response(CareRecordType type, Integer amountMl) {
        return new CareRecordResponse(
                UUID.fromString(RECORD_ID),
                UUID.fromString(BABY_ID),
                type,
                Instant.parse("2026-07-31T03:30:00Z"),
                amountMl,
                Instant.parse("2026-07-31T03:31:00Z"));
    }

    private static String requestJson(String type, Integer amountMl) {
        String amountField = amountMl == null ? "" : ",\"amountMl\":" + amountMl;
        return "{\"babyId\":\"" + BABY_ID + "\",\"type\":\"" + type
                + "\",\"recordedAt\":\"2026-07-31T03:30:00Z\"" + amountField + "}";
    }
}