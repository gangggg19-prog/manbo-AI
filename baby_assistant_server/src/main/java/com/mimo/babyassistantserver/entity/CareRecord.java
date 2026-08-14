package com.mimo.babyassistantserver.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * 育儿记录领域对象，对应 care_records 表的一行数据。
 * amountMl 仅对喂养记录有意义，尿布记录保持为空。
 */
public class CareRecord {
    private UUID id;
    private UUID babyId;
    private CareRecordType type;
    private Instant recordedAt;
    private Integer amountMl;
    private Instant createdAt;
    private Instant updatedAt;

    public CareRecord() {
    }

    public static CareRecord create(UUID babyId, CareRecordType type, Instant recordedAt, Integer amountMl) {
        CareRecord record = new CareRecord();
        Instant now = Instant.now();
        record.id = UUID.randomUUID();
        record.babyId = babyId;
        record.type = type;
        record.recordedAt = recordedAt;
        record.amountMl = amountMl;
        record.createdAt = now;
        record.updatedAt = now;
        return record;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBabyId() { return babyId; }
    public void setBabyId(UUID babyId) { this.babyId = babyId; }
    public CareRecordType getType() { return type; }
    public void setType(CareRecordType type) { this.type = type; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
    public Integer getAmountMl() { return amountMl; }
    public void setAmountMl(Integer amountMl) { this.amountMl = amountMl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}