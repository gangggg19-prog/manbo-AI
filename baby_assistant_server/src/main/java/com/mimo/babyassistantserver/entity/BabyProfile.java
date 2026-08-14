package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 宝宝档案领域对象，对应 baby_profiles 表的一行数据。
 * 不直接作为接口响应返回，外部输出通过 DTO 控制。
 */
public class BabyProfile {
    private UUID id;
    private String displayName;
    private LocalDate birthDate;
    private Instant createdAt;
    private Instant updatedAt;

    public BabyProfile() {
    }

    public static BabyProfile create(String displayName, LocalDate birthDate) {
        BabyProfile profile = new BabyProfile();
        Instant now = Instant.now();
        profile.id = UUID.randomUUID();
        profile.displayName = displayName;
        profile.birthDate = birthDate;
        profile.createdAt = now;
        profile.updatedAt = now;
        return profile;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}