package org.sopt.android1.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.record.dto.response.RecordCalendarResponse;
import org.sopt.android1.domain.record.service.RecordService;
import org.sopt.android1.global.response.ApiResponseBody;
import org.sopt.android1.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Archive", description = "나의 노하우집 도메인")
@RestController
@RequestMapping("/api/v1/me/records")
@RequiredArgsConstructor
public class RecordCalendarController {

    private static final String DEFAULT_YEAR =
            "#{T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Seoul')).getYear()}";
    private static final String DEFAULT_MONTH =
            "#{T(java.time.LocalDate).now(T(java.time.ZoneId).of('Asia/Seoul')).getMonthValue()}";

    private final RecordService recordService;

    @Operation(
            summary = "월별 캘린더 + 통계 조회",
            description = """
                    본인이 등록한 노하우 카드의 월별 요약을 조회합니다.

                    - MVP 기준 인증 없음 → 사용자 ID는 서버에서 1L로 고정 조회
                    - 일자 매핑은 카드의 createdAt 기준
                    - 한 날짜에 다수 기록이 있을 경우 최신 1건을 대표 카드로 응답
                    """
    )
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponseBody<RecordCalendarResponse, Void>> getCalendar(
            @Parameter(description = "조회 연도. 미전달 시 오늘 날짜 기준 연도")
            @RequestParam(defaultValue = DEFAULT_YEAR) int year,
            @Parameter(description = "조회 월(1~12). 미전달 시 오늘 날짜 기준 월")
            @RequestParam(defaultValue = DEFAULT_MONTH) int month
    ) {
        RecordCalendarResponse data = recordService.getCalendar(year, month);
        return ResponseEntity
                .status(SuccessCode.OK.getStatus())
                .body(ApiResponseBody.ok(SuccessCode.OK, data));
    }
}
