package org.sopt.android1.domain.record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "월별 캘린더 및 통계 응답")
public record RecordCalendarResponse(

        @Schema(description = "응답 연도", example = "2026")
        int year,

        @Schema(description = "응답 월", example = "5")
        int month,

        @Schema(description = "해당 월 기록 총 개수", example = "18")
        int recordCount,

        @Schema(description = "해당 월 또래 게시판 공유 개수", example = "7")
        int sharedCount,

        @Schema(description = "기록이 있는 일자별 대표 카드 목록")
        List<RecordCalendarDayResponse> days
) {
}
