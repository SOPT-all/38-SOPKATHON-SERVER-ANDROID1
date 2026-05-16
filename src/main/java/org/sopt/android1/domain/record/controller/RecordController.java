package org.sopt.android1.domain.record.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.home.dto.response.HomeRecordsResponse;
import org.sopt.android1.domain.record.service.RecordService;
import org.sopt.android1.global.response.ApiResponseBody;
import org.sopt.android1.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/records")
    public ResponseEntity<ApiResponseBody<HomeRecordsResponse, Void>> getSharedRecords() {
        return ResponseEntity
            .ok(ApiResponseBody.ok(SuccessCode.OK, recordService.getSharedRecords()));
    }
}
