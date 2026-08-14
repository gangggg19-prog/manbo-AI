package com.mimo.babyassistantserver.dto.care;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.CareRecord;
import com.mimo.babyassistantserver.entity.CareRecordType;

/**
 * 育儿记录的响应 DTO，供 Flutter 时间线和统计卡片使用。
 */
public record CareRecordResponse(UUID id, UUID babyId, CareRecordType type, Instant recordedAt, Integer amountMl, Instant createdAt) {
    public static CareRecordResponse from(CareRecord record) {
        return new CareRecordResponse(record.getId(), record.getBabyId(), record.getType(), record.getRecordedAt(), record.getAmountMl(), record.getCreatedAt());
    }
}