package com.mimo.babyassistantserver.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.care.CareRecordResponse;
import com.mimo.babyassistantserver.dto.care.CreateCareRecordRequest;
import com.mimo.babyassistantserver.entity.CareRecord;
import com.mimo.babyassistantserver.entity.CareRecordType;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.CareRecordMapper;
import com.mimo.babyassistantserver.service.CareRecordService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 育儿记录业务实现。
 * 负责业务校验、事务边界和错误语义；SQL 仅由 Mapper 层维护。
 */
@Service
public class CareRecordServiceImpl implements CareRecordService {
    private final CareRecordMapper careRecordMapper;
    private final BabyProfileMapper babyProfileMapper;

    public CareRecordServiceImpl(CareRecordMapper careRecordMapper, BabyProfileMapper babyProfileMapper) {
        this.careRecordMapper = careRecordMapper;
        this.babyProfileMapper = babyProfileMapper;
    }

    @Override
    @Transactional
    public CareRecordResponse create(CreateCareRecordRequest request) {
        validateAmount(request.type(), request.amountMl());
        if (babyProfileMapper.selectById(request.babyId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby profile was not found");
        }
        CareRecord record = CareRecord.create(request.babyId(), request.type(), request.recordedAt(), request.amountMl());
        careRecordMapper.insert(record);
        return CareRecordResponse.from(record);
    }

    /**
     * 将用户选择的本地日期转换为服务器时区的时间区间。
     * 使用左闭右开区间可保证相邻两天的记录不会重复或遗漏。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CareRecordResponse> list(UUID babyId, LocalDate date) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        ZoneId zone = ZoneId.systemDefault();
        Instant start = selectedDate.atStartOfDay(zone).toInstant();
        Instant end = selectedDate.plusDays(1).atStartOfDay(zone).toInstant();
        return careRecordMapper.selectByBabyIdAndRecordedAtBetween(babyId, start, end)
                .stream().map(CareRecordResponse::from).toList();
    }

    /**
     * 更新记录内容，但不允许通过更新接口改变其所属宝宝。
     * 该限制避免客户端传错 babyId 时造成跨家庭数据串联。
     */
    @Override
    @Transactional
    public CareRecordResponse update(UUID recordId, CreateCareRecordRequest request) {
        validateAmount(request.type(), request.amountMl());
        CareRecord record = getRequiredRecord(recordId);
        if (!record.getBabyId().equals(request.babyId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A record cannot be moved to another baby");
        }
        record.setType(request.type());
        record.setRecordedAt(request.recordedAt());
        record.setAmountMl(request.amountMl());
        record.setUpdatedAt(Instant.now());
        careRecordMapper.update(record);
        return CareRecordResponse.from(record);
    }

    @Override
    @Transactional
    public void delete(UUID recordId) {
        getRequiredRecord(recordId);
        careRecordMapper.deleteById(recordId);
    }

    /** 将不存在的记录统一转换为 HTTP 404，供更新和删除复用。 */
    /** 将不存在的记录统一转换为 HTTP 404，供更新和删除复用。 */
    private CareRecord getRequiredRecord(UUID recordId) {
        CareRecord record = careRecordMapper.selectById(recordId);
        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Care record was not found");
        }
        return record;
    }

    private void validateAmount(CareRecordType type, Integer amountMl) {
        if (type == CareRecordType.FEEDING && (amountMl == null || amountMl <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feeding records require an amount above zero");
        }
        if (type == CareRecordType.DIAPER && amountMl != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diaper records cannot include an amount");
        }
    }
}