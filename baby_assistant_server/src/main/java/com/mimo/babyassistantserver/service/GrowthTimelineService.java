package com.mimo.babyassistantserver.service;

import java.util.UUID;

import com.mimo.babyassistantserver.dto.timeline.GrowthTimelineResponse;

/** Returns a short, ordered history for the growth tab. */
public interface GrowthTimelineService {
    GrowthTimelineResponse get(UUID babyId, int days);
}