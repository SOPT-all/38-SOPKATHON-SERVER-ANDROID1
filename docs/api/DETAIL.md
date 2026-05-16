# DETAIL 상세 페이지 API 명세

---

## API 1. 노하우 카드 상세 조회

캘린더 셀 탭으로 진입한 상세 페이지에 표시할 단일 카드 정보를 조회한다.

**GET** `/api/v1/records/{recordId}`

### Path Parameter

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| recordId | Long | Y | 노하우 카드 ID | `1` |

### Response Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| success | Boolean | Y | 요청 성공 여부 | `true` |
| status | Integer | Y | HTTP 상태 코드 | `200` |
| message | String | Y | 응답 메시지 | `"요청이 성공했습니다."` |
| data | Object | Y | 응답 데이터 | - |
| data.recordId | Long | Y | 노하우 카드 ID | `1` |
| data.title | String | Y | 기록 제목 | `"상추 모종 심기"` |
| data.photoUrl | String | N | 사진 URL (없을 시 `null`) | `"https://.../lettuce.jpg"` |
| data.isShared | Boolean | Y | 또래 게시판 공유 여부 | `false` |
| data.createdAt | String (ISO-8601) | Y | 서버 저장 시각 (화면 상단 타이틀 노출 기준) | `"2026-05-14T16:19:02+09:00"` |

> **응답에 포함되지 않는 필드 (의도적 제외)**
> - `location` — 위치 정보 미저장 (MVP 범위 외).
> - `recordedAt` — 사진 EXIF 촬영 시각 미저장. 날짜는 `createdAt` 으로 갈음.
> - `voiceUrl` / `voiceDurationSeconds` — 음성 파일 서버 미보관, 상세 페이지 음성 플레이어 MVP 제외.
> - `content` — 별도 컬럼 자체가 없음. STT 변환문은 `title` 에 흡수되어 저장됨.
> - `likeCount` / `commentCount` — 상세에서는 카운트 미노출. HOME 카드 응답에만 포함.

### Success Response Example

**200 OK**

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공했습니다.",
  "data": {
    "recordId": 1,
    "title": "상추 모종 심기",
    "photoUrl": "https://cdn.example.com/records/1/lettuce.jpg",
    "isShared": false,
    "createdAt": "2026-05-14T16:19:02+09:00"
  }
}
```

### Error Response Example

**404 Not Found — 존재하지 않는 카드**

```json
{
  "success": false,
  "status": 404,
  "message": "존재하지 않는 리소스입니다.",
  "code": "COM_404_001",
  "meta": {
    "path": "/api/v1/records/1",
    "timestamp": "2026-05-14T16:19:02+09:00"
  }
}
```

**500 Internal Server Error**

```json
{
  "success": false,
  "status": 500,
  "message": "서버 내부 오류가 발생했습니다.",
  "code": "COM_500_001",
  "meta": {
    "path": "/api/v1/records/1",
    "timestamp": "2026-05-14T16:19:02+09:00"
  }
}
```

---
