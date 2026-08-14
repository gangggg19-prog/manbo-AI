package com.mimo.babyassistantserver.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mimo.babyassistantserver.dto.care.CareRecordResponse;
import com.mimo.babyassistantserver.dto.care.CreateCareRecordRequest;
import com.mimo.babyassistantserver.service.CareRecordService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 育儿记录的 HTTP 入口层。
 * Flutter 通过这里新增、查询、编辑和删除记录，Controller 本身不直接访问数据库。
 */
@RestController
@RequestMapping("/api/v1/care-records")
public class CareRecordController {
    private final CareRecordService careRecordService;

    public CareRecordController(CareRecordService careRecordService) {
        this.careRecordService = careRecordService;
    }

    /** 新增一条育儿记录，成功后返回 201 Created。 */
    @PostMapping
    public ResponseEntity<CareRecordResponse> create(@Valid @RequestBody CreateCareRecordRequest request) {
        CareRecordResponse response = careRecordService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/care-records/" + response.id())).body(response);
    }

    /** 查询指定宝宝某一天的全部记录，供首页统计和时间线读取。 */
    @GetMapping
    public List<CareRecordResponse> list(
            @RequestParam UUID babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return careRecordService.list(babyId, date);
    }

    /** 修改已有记录，记录 ID 来自路径，内容来自请求体。 */
    @PutMapping("/{recordId}")
    public CareRecordResponse update(
            @PathVariable UUID recordId,
            @Valid @RequestBody CreateCareRecordRequest request) {
        return careRecordService.update(recordId, request);
    }

    /** 删除一条记录，成功后只返回 204，不返回响应体。 */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> delete(@PathVariable UUID recordId) {
        careRecordService.delete(recordId);
        return ResponseEntity.noContent().build();
    }
}