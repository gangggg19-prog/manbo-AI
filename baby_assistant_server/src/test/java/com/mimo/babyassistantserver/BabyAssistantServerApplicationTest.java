package com.mimo.babyassistantserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mimo.babyassistantserver.entity.BabyProfile;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.mapper.CareRecordMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class BabyAssistantServerApplicationTest {

    @Autowired
    private BabyProfileMapper babyProfileMapper;

    @Autowired
    private CareRecordMapper careRecordMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void mapperCanReadBabyProfiles() {
        assertNotNull(babyProfileMapper.selectAll());
    }

    @Test
    void mapperMapsRecordTypeWhenRecordsExist() {
        List<BabyProfile> babies = babyProfileMapper.selectAll();
        if (babies.isEmpty()) {
            return;
        }
        var records = careRecordMapper.selectByBabyIdAndRecordedAtBetween(
                babies.get(0).getId(),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2027-01-01T00:00:00Z"));
        assertTrue(records.stream().noneMatch(record -> record.getType() == null));
    }
}