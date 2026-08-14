package com.mimo.babyassistantserver.controller;

import java.net.URI;
import java.util.List;

import com.mimo.babyassistantserver.dto.baby.BabyProfileResponse;
import com.mimo.babyassistantserver.dto.baby.CreateBabyProfileRequest;
import com.mimo.babyassistantserver.service.BabyProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 宝宝档案的 HTTP 入口层。
 * 本层只负责请求参数、HTTP 状态码和响应格式，业务规则交给 Service。
 */
@RestController
@RequestMapping("/api/v1/babies")
public class BabyProfileController {
    private final BabyProfileService babyProfileService;

    public BabyProfileController(BabyProfileService babyProfileService) {
        this.babyProfileService = babyProfileService;
    }

    @PostMapping
    public ResponseEntity<BabyProfileResponse> create(@Valid @RequestBody CreateBabyProfileRequest request) {
        BabyProfileResponse response = babyProfileService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/babies/" + response.id())).body(response);
    }

    @GetMapping
    public List<BabyProfileResponse> list() {
        return babyProfileService.list();
    }
}