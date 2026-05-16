package org.sopt.android1.domain.record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.android1.domain.record.entity.RecordEntity;

@Schema(description = "월별 캘린더 일자 응답")
public record RecordCalendarDayResponse(

        @Schema(description = "일", example = "14")
        int day,

        @Schema(description = "해당 일의 대표 카드 ID", example = "1")
        Long recordId,

        @Schema(description = "셀 배경 썸네일 URL. 사진 없을 시 null", example = "/dummy/p1.png", nullable = true)
        String thumbnailUrl
) {

    public static RecordCalendarDayResponse from(RecordEntity entity) {
        return new RecordCalendarDayResponse(
                entity.getCreatedAt().getDayOfMonth(),
                entity.getId(),
                entity.getPhotoUrl()
        );
    }
}
