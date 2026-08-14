package com.mimo.babyassistantserver.service.impl;

import java.util.List;

import com.mimo.babyassistantserver.dto.baby.BabyProfileResponse;
import com.mimo.babyassistantserver.dto.baby.CreateBabyProfileRequest;
import com.mimo.babyassistantserver.entity.BabyProfile;
import com.mimo.babyassistantserver.mapper.BabyProfileMapper;
import com.mimo.babyassistantserver.service.BabyProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宝宝档案业务实现。
 * 负责创建领域对象、开启事务，并通过 Mapper 持久化到 PostgreSQL。
 */
@Service
public class BabyProfileServiceImpl implements BabyProfileService {
    private final BabyProfileMapper babyProfileMapper;

    public BabyProfileServiceImpl(BabyProfileMapper babyProfileMapper) {
        this.babyProfileMapper = babyProfileMapper;
    }

    @Override
    @Transactional
    public BabyProfileResponse create(CreateBabyProfileRequest request) {
        BabyProfile profile = BabyProfile.create(request.displayName().trim(), request.birthDate());
        babyProfileMapper.insert(profile);
        return BabyProfileResponse.from(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BabyProfileResponse> list() {
        return babyProfileMapper.selectAll().stream().map(BabyProfileResponse::from).toList();
    }
}