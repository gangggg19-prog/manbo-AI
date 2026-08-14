package com.mimo.babyassistantserver.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.summary.DailySummaryResponse;
import com.mimo.babyassistantserver.service.DailySummaryService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for the real-data home screen briefing. */
@RestController
@RequestMapping("/api/v1/daily-summary")
public class DailySummaryController {
    private final DailySummaryService dailySummaryService;

    public DailySummaryController(DailySummaryService dailySummaryService) {
        this.dailySummaryService = dailySummaryService;
    }

    @GetMapping
    public DailySummaryResponse get(
            @RequestParam UUID babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailySummaryService.get(babyId, date);
    }
}