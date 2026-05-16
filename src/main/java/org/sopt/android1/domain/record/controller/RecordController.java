package org.sopt.android1.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.record.response.HomeRecordsResponse;
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
@Tag(name = "Record", description = "기록 API")
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "홈 공유 기록 목록 조회", description = "홈 화면에 노출할 공유된 기록 목록을 최신순으로 조회합니다.")
    @GetMapping("/records")
    public ResponseEntity<ApiResponseBody<HomeRecordsResponse, Void>> getSharedRecords() {
        return ResponseEntity
            .ok(ApiResponseBody.ok(SuccessCode.OK, recordService.getSharedRecords()));
    }
}
