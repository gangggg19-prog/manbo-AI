package com.mimo.babyassistantserver.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.sleep.EndSleepSessionRequest;
import com.mimo.babyassistantserver.dto.sleep.SleepSessionResponse;
import com.mimo.babyassistantserver.dto.sleep.StartSleepSessionRequest;
import com.mimo.babyassistantserver.service.SleepSessionService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for sleep-session actions from Flutter. */
@RestController
@RequestMapping("/api/v1/sleep-sessions")
public class SleepSessionController {
    private final SleepSessionService sleepSessionService;

    public SleepSessionController(SleepSessionService sleepSessionService) {
        this.sleepSessionService = sleepSessionService;
    }

    @PostMapping
    public ResponseEntity<SleepSessionResponse> start(@Valid @RequestBody StartSleepSessionRequest request) {
        SleepSessionResponse response = sleepSessionService.start(request);
        return ResponseEntity.created(URI.create("/api/v1/sleep-sessions/" + response.id())).body(response);
    }

    @PatchMapping("/{sessionId}/end")
    public SleepSessionResponse end(
            @PathVariable UUID sessionId,
            @Valid @RequestBody EndSleepSessionRequest request) {
        return sleepSessionService.end(sessionId, request);
    }

    @GetMapping
    public List<SleepSessionResponse> list(
            @RequestParam UUID babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return sleepSessionService.list(babyId, date);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID sessionId) {
        sleepSessionService.delete(sessionId);
        return ResponseEntity.noContent().build();
    }
}