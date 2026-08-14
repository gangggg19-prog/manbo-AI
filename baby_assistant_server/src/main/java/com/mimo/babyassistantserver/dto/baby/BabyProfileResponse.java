package com.mimo.babyassistantserver.dto.baby;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.BabyProfile;

/**
 * 宝宝档案的响应 DTO。
 * 用 DTO 隔离数据库对象，避免表结构变化直接影响 API。
 */
public record BabyProfileResponse(UUID id, String displayName, LocalDate birthDate, Instant createdAt) {
    public static BabyProfileResponse from(BabyProfile profile) {
        return new BabyProfileResponse(profile.getId(), profile.getDisplayName(), profile.getBirthDate(), profile.getCreatedAt());
    }
}