package com.mimo.babyassistantserver.dto.baby;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建宝宝档案的入参 DTO。
 * Bean Validation 在进入业务层前完成基础格式校验。
 */
public record CreateBabyProfileRequest(
        @NotBlank(message = "宝宝昵称不能为空")
        @Size(max = 80, message = "宝宝昵称不能超过 80 个字符")
        String displayName,
        LocalDate birthDate) {
}