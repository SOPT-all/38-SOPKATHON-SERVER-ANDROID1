package org.sopt.android1.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.sopt.android1.domain.record.entity.RecordEntity;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "노하우 기록 등록 응답")
public record RecordCreateResponse(

        @Schema(description = "생성된 노하우 카드 ID", example = "1")
        Long recordId,

        @Schema(description = "기록 제목", example = "상추 모종 심기")
        String title,

        @Schema(description = "첨부 사진 URL (미첨부 시 null)", example = "/uploads/9d3e8f1a-....jpg", nullable = true)
        String photoUrl,

        @Schema(description = "녹음 길이(초). 없을 시 null", example = "30", nullable = true)
        Integer voiceDurationSeconds,

        @Schema(description = "또래 게시판 공유 여부", example = "false")
        boolean isShared,

        @Schema(description = "한글 주소 문자열", example = "서울시 마포구 망원동")
        String location,

        @Schema(description = "EXIF 촬영 시각 (KST)", example = "2026-05-14T16:19:02+09:00")
        OffsetDateTime recordedAt,

        @Schema(description = "서버 저장 시각 (KST)", example = "2026-05-14T16:19:02+09:00")
        OffsetDateTime createdAt
) {

    public static RecordCreateResponse of(
            RecordEntity entity,
            OffsetDateTime recordedAt,
            OffsetDateTime createdAt
    ) {
        return new RecordCreateResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPhotoUrl(),
                entity.getVoiceDurationSeconds(),
                entity.isShared(),
                entity.getLocation(),
                recordedAt,
                createdAt
        );
    }
}
