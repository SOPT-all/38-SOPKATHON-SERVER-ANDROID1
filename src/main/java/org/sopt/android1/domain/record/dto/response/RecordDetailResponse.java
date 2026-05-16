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

        @Schema(description = "서버 저장 시각 (KST) — 화면 상단 타이틀 노출 기준", example = "2026-05-14T16:19:02+09:00")
        OffsetDateTime createdAt
) {

    public static RecordDetailResponse of(RecordEntity entity, OffsetDateTime createdAt) {
        return new RecordDetailResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPhotoUrl(),
                entity.isShared(),
                createdAt
        );
    }
}
