package org.sopt.android1.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.sopt.android1.domain.record.entity.RecordEntity;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "노하우 카드 상세 조회 응답")
public record RecordDetailResponse(

        @Schema(description = "노하우 카드 ID", example = "1")
        Long recordId,

        @Schema(description = "기록 제목", example = "상추 모종 심기")
        String title,

        @Schema(description = "첨부 사진 URL (미첨부 시 null)", example = "/uploads/9d3e8f1a-....jpg", nullable = true)
        String photoUrl,

        @Schema(description = "또래 게시판 공유 여부", example = "false")
        boolean isShared,

        @Schema(description = "사진 촬영 날짜 라벨 — DETAIL 상단 타이틀 노출용 (recordedAt 의 M월 d일)", example = "5월 14일")
        String dateLabel,

        @Schema(description = "사진 촬영 시각 라벨 (recordedAt 의 HH:mm)", example = "07:50")
        String timeLabel,

        @Schema(description = "촬영 위치 라벨 — DETAIL 위치 칩 노출 (시·도 접미사 제거 + 첫 2 토큰)", example = "서울 노원구")
        String locationLabel,

        @Schema(description = "음성 녹음 길이 라벨 (M:SS 형식). 미첨부 시 null", example = "0:48", nullable = true)
        String voiceDurationLabel,

        @Schema(description = "서버 저장 시각 (KST)", example = "2026-05-14T16:19:02+09:00")
        OffsetDateTime createdAt
) {

    public static RecordDetailResponse of(
            RecordEntity entity,
            String dateLabel,
            String timeLabel,
            String locationLabel,
            String voiceDurationLabel,
            OffsetDateTime createdAt
    ) {
        return new RecordDetailResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPhotoUrl(),
                entity.isShared(),
                dateLabel,
                timeLabel,
                locationLabel,
                voiceDurationLabel,
                createdAt
        );
    }
}
