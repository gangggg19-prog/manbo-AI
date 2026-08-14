package com.mimo.babyassistantserver.service;

import java.util.List;

import com.mimo.babyassistantserver.dto.baby.BabyProfileResponse;
import com.mimo.babyassistantserver.dto.baby.CreateBabyProfileRequest;

/**
 * 宝宝档案的业务能力定义。
 * Controller 只依赖接口，具体实现可替换而不影响调用方。
 */
public interface BabyProfileService {
    BabyProfileResponse create(CreateBabyProfileRequest request);
    List<BabyProfileResponse> list();
}