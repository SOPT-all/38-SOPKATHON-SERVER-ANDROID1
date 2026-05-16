package org.sopt.android1.domain.record.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.record.dto.request.RecordCreateRequest;
import org.sopt.android1.domain.record.dto.response.RecordCreateResponse;
import org.sopt.android1.domain.record.service.RecordService;
import org.sopt.android1.global.response.ApiResponseBody;
import org.sopt.android1.global.response.SuccessCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Record", description = "노하우 기록 도메인 — 등록 / 조회")
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @Operation(
            summary = "노하우 기록 등록",
            description = """
                    부모 사용자가 사진 · 제목 · 메타데이터를 묶어 노하우 카드를 등록합니다.

                    - **Content-Type:** `multipart/form-data`
                    - **photo:** 첨부 시 서버가 `./uploads/{uuid}.{ext}` 로 저장, 응답 `photoUrl` 은 `/uploads/{uuid}.{ext}` (정적 서빙됨)
                    - **location / recordedAt:** 안드로 클라가 사진 EXIF (GPS / DateTimeOriginal) 에서 추출해 함께 전송. DETAIL 화면의 위치 칩 / 타이틀 일자가 이 값들 기준으로 노출됨
                    - **voiceDurationSeconds:** 또래 게시판 카드 우상단 칩에 노출. 음성 본문·STT 결과는 서버 미수신
                    - **isShared:** `true` 면 또래 게시판 공유, `false` 면 본인 저장만
                    - **userId:** MVP 기준 인증 없음 → 서버에서 `1L` (박순자) 로 고정 저장
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "노하우 카드 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseBody.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "status": 201,
                                              "message": "리소스가 생성되었습니다.",
                                              "data": {
                                                "recordId": 1,
                                                "title": "상추 모종 심기",
                                                "photoUrl": "/uploads/9d3e8f1a-1a2b-4c3d-9e0f-7a6b5c4d3e2f.jpg",
                                                "voiceDurationSeconds": 30,
                                                "isShared": false,
                                                "location": "서울시 마포구 망원동",
                                                "recordedAt": "2026-05-14T16:19:02+09:00",
                                                "createdAt": "2026-05-14T16:19:02+09:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "빈 값 / 잘못된 입력 (필수 필드 누락, ISO-8601 형식 위반 등)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "빈 값 입력",
                                    value = """
                                            {
                                              "success": false,
                                              "status": 400,
                                              "message": "빈 값은 허용되지 않습니다.",
                                              "code": "COM_400_004",
                                              "meta": {
                                                "path": "/api/v1/records",
                                                "timestamp": "2026-05-14T16:19:02+09:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 (파일 저장 실패 등)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "success": false,
                                              "status": 500,
                                              "message": "서버 내부 오류가 발생했습니다.",
                                              "code": "COM_500_001",
                                              "meta": {
                                                "path": "/api/v1/records",
                                                "timestamp": "2026-05-14T16:19:02+09:00"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseBody<RecordCreateResponse, Void>> create(
            @Valid @ModelAttribute RecordCreateRequest request
    ) {
        RecordCreateResponse data = recordService.create(request);
        return ResponseEntity
                .status(SuccessCode.CREATED.getStatus())
                .body(ApiResponseBody.created(SuccessCode.CREATED, data));
    }
}