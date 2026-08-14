package com.mimo.babyassistantserver.dto.familychat;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateFamilyChatRoomRequest(@NotNull UUID babyId) {}