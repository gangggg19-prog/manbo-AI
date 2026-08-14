package com.mimo.babyassistantserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.baby.BabyProfileResponse;
import com.mimo.babyassistantserver.service.BabyProfileService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BabyProfileController.class)
class BabyProfileControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private BabyProfileService babyProfileService;

    @Test
    void createsABabyProfile() throws Exception {
        BabyProfileResponse response = new BabyProfileResponse(UUID.fromString("8a307dc0-2e09-4d12-8eee-0f9d9574ee25"), "小满", LocalDate.of(2026, 4, 1), Instant.parse("2026-07-31T03:00:00Z"));
        given(babyProfileService.create(any())).willReturn(response);
        mockMvc.perform(post("/api/v1/babies").contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"小满\",\"birthDate\":\"2026-04-01\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.displayName").value("小满"));
    }

    @Test
    void listsBabyProfiles() throws Exception {
        BabyProfileResponse response = new BabyProfileResponse(UUID.fromString("8a307dc0-2e09-4d12-8eee-0f9d9574ee25"), "小满", LocalDate.of(2026, 4, 1), Instant.parse("2026-07-31T03:00:00Z"));
        given(babyProfileService.list()).willReturn(List.of(response));
        mockMvc.perform(get("/api/v1/babies")).andExpect(status().isOk()).andExpect(jsonPath("$[0].displayName").value("小满"));
    }
}