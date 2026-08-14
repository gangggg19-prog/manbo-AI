package com.mimo.babyassistantserver.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.ai.AiChatResponse;
import com.mimo.babyassistantserver.dto.ai.AiConversationResponse;
import com.mimo.babyassistantserver.dto.ai.AiMessageResponse;
import com.mimo.babyassistantserver.entity.AiMessageRole;
import com.mimo.babyassistantserver.service.AiAssistantService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiAssistantController.class)
class AiAssistantControllerTest {
    private static final String BABY_ID = "618b988e-9588-4c64-9da8-b010c52e3f40";
    private static final String CONVERSATION_ID = "ea6647df-813e-42b5-93b3-9a6eb7e80c1a";

    @Autowired private MockMvc mockMvc;
    @MockBean private AiAssistantService aiAssistantService;

    @Test
    void forwardsAQuestionWithItsConversation() throws Exception {
        given(aiAssistantService.chat(org.mockito.ArgumentMatchers.any()))
                .willReturn(new AiChatResponse(
                        UUID.fromString(CONVERSATION_ID), "今天已经有真实记录。", "仅供日常参考。",
                        "qwen", List.of("查看今日简报"), List.of()));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"babyId\":\"" + BABY_ID + "\",\"conversationId\":\"" + CONVERSATION_ID
                                + "\",\"message\":\"今天睡得怎么样？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.source").value("qwen"));
    }

    @Test
    void createsAndLoadsConversationHistory() throws Exception {
        given(aiAssistantService.createConversation(UUID.fromString(BABY_ID)))
                .willReturn(new AiConversationResponse(
                        UUID.fromString(CONVERSATION_ID), UUID.fromString(BABY_ID), Instant.parse("2026-08-03T00:00:00Z")));
        given(aiAssistantService.messages(UUID.fromString(CONVERSATION_ID)))
                .willReturn(List.of(new AiMessageResponse(
                        UUID.fromString("1b447c95-2b63-497f-9155-fc66d68e4b90"), UUID.fromString(CONVERSATION_ID),
                        AiMessageRole.USER, "第一句问题", null, Instant.parse("2026-08-03T00:00:00Z"))));

        mockMvc.perform(post("/api/v1/ai/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"babyId\":\"" + BABY_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CONVERSATION_ID));
        mockMvc.perform(get("/api/v1/ai/conversations/{id}/messages", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].content").value("第一句问题"));
    }

    @Test
    void rejectsAnEmptyQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"babyId\":\"" + BABY_ID + "\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}