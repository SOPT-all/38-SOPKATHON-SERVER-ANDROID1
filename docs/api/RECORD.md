# RECORD 기록 API 명세

---

## API 1. 노하우 기록 등록

부모 사용자가 사진 · 제목 · 메타데이터를 묶어 노하우 카드를 등록한다.
음성 녹음은 클라에서만 처리되며 (STT 결과 = 클라 하드코딩 `"안녕하세요"`), 서버는 음성 본문을 받지 않는다.
`isShared` 값으로 또래 게시판 게시 여부를 결정한다.

**POST** `/api/v1/records`

### Request Header

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| Content-Type | String | Y | multipart/form-data | `multipart/form-data` |

### Request Body

`multipart/form-data`

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| title | String | Y | 사용자가 직접 입력하는 기록 제목 (디자인 002 의 제목 텍스트 필드) | `"상추 모종 심기"` |
| photo | File | N | 첨부 사진 (단일 이미지) | `image/jpeg` |
| voiceDurationSeconds | Integer | N | 녹음 길이(초). 또래 게시판 카드 우상단 칩에 노출됨 | `30` |
| isShared | Boolean | Y | 또래 게시판 공유 여부 (`true` = 공유 / `false` = 저장만) | `false` |

> 음성 파일·STT 결과는 서버에 전송하지 않는다 — 길이(`voiceDurationSeconds`) 만 전송한다. STT 결과는 클라가 `"안녕하세요"` 로 하드코딩한다.
> 위치 / 촬영 시각 정보는 MVP 범위에서 저장하지 않는다. 디자인의 위치 칩은 MVP 제외, 날짜는 서버 `createdAt` 으로 노출한다.

### Request Example

```
POST /api/v1/records
Content-Type: multipart/form-data; boundary=----Boundary

------Boundary
Content-Disposition: form-data; name="title"

상추 모종 심기
------Boundary
Content-Disposition: form-data; name="photo"; filename="lettuce.jpg"
Content-Type: image/jpeg

<binary>
------Boundary
Content-Disposition: form-data; name="voiceDurationSeconds"

30
------Boundary
Content-Disposition: form-data; name="isShared"

false
------Boundary--
```

### Response Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| success | Boolean | Y | 요청 성공 여부 | `true` |
| status | Integer | Y | HTTP 상태 코드 | `201` |
| message | String | Y | 응답 메시지 | `"리소스가 생성되었습니다."` |
| data | Object | Y | 응답 데이터 | - |
| data.recordId | Long | Y | 생성된 노하우 카드 ID | `1` |
| data.title | String | Y | 기록 제목 | `"상추 모종 심기"` |
| data.photoUrl | String | N | 첨부 사진 URL (미첨부 시 `null`) | `"https://.../lettuce.jpg"` |
| data.voiceDurationSeconds | Integer | N | 녹음 길이(초). 없을 시 `null` | `30` |
| data.isShared | Boolean | Y | 또래 게시판 공유 여부 | `false` |
| data.createdAt | String (ISO-8601) | Y | 서버 저장 시각 | `"2026-05-14T16:19:02+09:00"` |

### Success Response Example

**201 Created**

```json
{
  "success": true,
  "status": 201,
  "message": "리소스가 생성되었습니다.",
  "data": {
    "recordId": 1,
    "title": "상추 모종 심기",
    "photoUrl": "https://cdn.example.com/records/1/lettuce.jpg",
    "voiceDurationSeconds": 30,
    "isShared": false,
    "createdAt": "2026-05-14T16:19:02+09:00"
  }
}
```

### Error Response Example

**400 Bad Request — 빈 값 입력**

```json
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
```

**500 Internal Server Error**

```json
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
```

---
