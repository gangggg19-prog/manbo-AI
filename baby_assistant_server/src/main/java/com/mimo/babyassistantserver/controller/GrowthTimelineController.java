package com.mimo.babyassistantserver.controller;

import java.util.UUID;

import com.mimo.babyassistantserver.dto.timeline.GrowthTimelineResponse;
import com.mimo.babyassistantserver.service.GrowthTimelineService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for the recent growth history. */
@RestController
@Validated
@RequestMapping("/api/v1/growth-timeline")
public class GrowthTimelineController {
    private final GrowthTimelineService growthTimelineService;

    public GrowthTimelineController(GrowthTimelineService growthTimelineService) {
        this.growthTimelineService = growthTimelineService;
    }

    @GetMapping
    public GrowthTimelineResponse get(
            @RequestParam UUID babyId,
            @RequestParam(defaultValue = "7") @Min(1) @Max(31) int days) {
        return growthTimelineService.get(babyId, days);
    }
}