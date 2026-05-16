package org.sopt.android1.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "노하우 기록 등록 요청 (multipart/form-data)")
public record RecordCreateRequest(

        @Schema(
                description = "사용자가 직접 입력하는 기록 제목 (디자인 002 의 제목 텍스트 필드 입력값)",
                example = "상추 모종 심기",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String title,

        @Schema(
                description = "첨부 사진 (단일 이미지). 미첨부 가능. 첨부 시 서버가 `./uploads/{uuid}.{ext}` 로 저장 후 `/uploads/{uuid}.{ext}` 로 서빙",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        MultipartFile photo,

        @Schema(
                description = "녹음 길이(초). 또래 게시판 카드 우상단 칩에 노출됨. 음성 본문·STT 결과는 서버 미수신",
                example = "30",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Integer voiceDurationSeconds,

        @Schema(
                description = "또래 게시판 공유 여부 (`true` = 공유 / `false` = 저장만)",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Boolean isShared,

        @Schema(
                description = "안드로 클라가 EXIF GPS 를 변환해 보내는 한글 주소 문자열. DETAIL 위치 칩에 그대로 노출",
                example = "서울시 마포구 망원동",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String location,

        @Schema(
                description = "EXIF DateTimeOriginal — 사진 촬영 시각 (ISO-8601, KST 권장). DETAIL 타이틀 / ARCHIVE 캘린더 일자 매핑 기준",
                example = "2026-05-14T16:19:02+09:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime recordedAt
) {
}