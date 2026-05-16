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
| data.dateLabel | String | Y | 사진 촬영 날짜 라벨 (`recordedAt` 의 `M월 d일`) — DETAIL 상단 타이틀 노출 | `"5월 14일"` |
| data.timeLabel | String | Y | 사진 촬영 시각 라벨 (`recordedAt` 의 `HH:mm`) | `"07:50"` |
| data.locationLabel | String | Y | 위치 라벨 (시·도 접미사 제거 + 첫 2 토큰) — DETAIL 위치 칩 노출 | `"서울 노원구"` |
| data.voiceDurationLabel | String | N | 음성 녹음 길이 라벨 (`M:SS` 형식). 미첨부 시 `null` | `"0:48"` |
| data.createdAt | String (ISO-8601) | Y | 서버 저장 시각 (KST) | `"2026-05-14T16:19:02+09:00"` |

> **응답에 포함되지 않는 필드 (의도적 제외)**
> - `voiceUrl` — 음성 파일 서버 미보관 (길이만 보관).
> - `content` — 별도 컬럼 자체가 없음. STT 변환문은 클라가 하드코딩으로 노출.
> - `likeCount` / `commentCount` — 상세에서는 카운트 미노출. HOME 카드 응답에만 포함.

### 라벨 변환 규칙

| 라벨 | 원본 | 변환 규칙 |
|------|------|-----------|
| `dateLabel` | `record.recorded_at` | `DateTimeFormatter("M'월' d'일'", Locale.KOREAN)` |
| `timeLabel` | `record.recorded_at` | `DateTimeFormatter("HH:mm")` |
| `locationLabel` | `record.location` | 공백 분리 후 첫 토큰을 시·도 매핑표로 단축 + 두 번째 토큰 그대로 (예: `서울시` → `서울`, `제주특별자치도` → `제주`, `전라남도` → `전남`). 단일 토큰이면 단축된 토큰만 |
| `voiceDurationLabel` | `record.voice_duration_seconds` | `M:SS` (`30` → `0:30`, `90` → `1:30`, `null` → `null`) |

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
    "dateLabel": "5월 14일",
    "timeLabel": "07:50",
    "locationLabel": "서울 마포구",
    "voiceDurationLabel": "0:30",
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
