package com.mimo.babyassistantserver.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.care.CreateCareRecordRequest;
import com.mimo.babyassistantserver.entity.CareRecord;
import com.mimo.babyassistantserver.entity.CareRecordType;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.CareRecordMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CareRecordServiceImplTest {
    @Mock private CareRecordMapper careRecordMapper;
    @Mock private BabyProfileMapper babyProfileMapper;

    @Test
    void rejectsFeedingWithoutAmount() {
        CareRecordServiceImpl service = new CareRecordServiceImpl(careRecordMapper, babyProfileMapper);
        assertThrows(ResponseStatusException.class, () -> service.create(new CreateCareRecordRequest(null, CareRecordType.FEEDING, null, null)));
    }

    @Test
    void rejectsDiaperWithAmount() {
        CareRecordServiceImpl service = new CareRecordServiceImpl(careRecordMapper, babyProfileMapper);
        assertThrows(ResponseStatusException.class, () -> service.create(new CreateCareRecordRequest(null, CareRecordType.DIAPER, null, 50)));
    }

    @Test
    void updatesAnExistingRecord() {
        UUID babyId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        CareRecord record = CareRecord.create(babyId, CareRecordType.FEEDING, Instant.parse("2026-07-31T03:00:00Z"), 120);
        record.setId(recordId);
        when(careRecordMapper.selectById(recordId)).thenReturn(record);
        CareRecordServiceImpl service = new CareRecordServiceImpl(careRecordMapper, babyProfileMapper);

        var response = service.update(recordId, new CreateCareRecordRequest(
                babyId, CareRecordType.FEEDING, Instant.parse("2026-07-31T04:00:00Z"), 150));

        assertEquals(150, response.amountMl());
        verify(careRecordMapper).update(record);
    }

    @Test
    void deletesAnExistingRecord() {
        UUID recordId = UUID.randomUUID();
        when(careRecordMapper.selectById(recordId)).thenReturn(CareRecord.create(
                UUID.randomUUID(), CareRecordType.DIAPER, Instant.parse("2026-07-31T03:00:00Z"), null));
        CareRecordServiceImpl service = new CareRecordServiceImpl(careRecordMapper, babyProfileMapper);

        service.delete(recordId);

        verify(careRecordMapper).deleteById(recordId);
    }
}