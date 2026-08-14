package com.mimo.babyassistantserver.dto.care;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.entity.CareRecordType;

import jakarta.validation.constraints.NotNull;

/**
 * 新增或更新育儿记录的入参 DTO。
 * 更新时仍携带 babyId，用于阻止记录被错误移动到另一个宝宝。
 */
public record CreateCareRecordRequest(
        @NotNull(message = "宝宝 ID 不能为空") UUID babyId,
        @NotNull(message = "记录类型不能为空") CareRecordType type,
        @NotNull(message = "记录时间不能为空") Instant recordedAt,
        Integer amountMl) {
}