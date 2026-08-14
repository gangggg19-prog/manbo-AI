package com.mimo.babyassistantserver.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.care.CareRecordResponse;
import com.mimo.babyassistantserver.dto.care.CreateCareRecordRequest;

/**
 * 育儿记录的业务能力定义。
 * 这里描述“能做什么”，具体校验、事务与数据访问由 Impl 完成。
 */
public interface CareRecordService {
    CareRecordResponse create(CreateCareRecordRequest request);
    List<CareRecordResponse> list(UUID babyId, LocalDate date);
    CareRecordResponse update(UUID recordId, CreateCareRecordRequest request);
    void delete(UUID recordId);
}