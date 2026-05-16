# HOME 또래 공유 게시판 API 명세

---

## API 1. 또래 공유 게시판 카드 목록 조회

또래 부모 사용자들이 `isShared=true` 로 게시한 노하우 카드 전체를 한 번에 조회한다.

**GET** `/api/v1/home/records`

> 정렬은 최신순(`createdAt` 내림차순) 고정. MVP 범위에서는 페이지네이션을 적용하지 않고 전체 카드를 반환한다.

### Response Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| success | Boolean | Y | 요청 성공 여부 | `true` |
| status | Integer | Y | HTTP 상태 코드 | `200` |
| message | String | Y | 응답 메시지 | `"요청이 성공했습니다."` |
| data | Object | Y | 응답 데이터 | - |
| data.records | Array | Y | 노하우 카드 배열 | - |
| data.records[].recordId | Long | Y | 노하우 카드 ID | `1` |
| data.records[].author | Object | Y | 작성자 정보 | - |
| data.records[].author.name | String | Y | 작성자 이름 (`"{이름}님"` 형식은 클라가 조립) | `"박순자"` |
| data.records[].author.age | Integer | Y | 작성자 나이 | `68` |
| data.records[].title | String | Y | 기록 제목 | `"상추 모종 심을 땐 아침에만 물을 줘야 해요"` |
| data.records[].photoUrl | String | N | 사진 URL (없을 시 `null`) | `"https://.../lettuce.jpg"` |
| data.records[].voiceDurationSeconds | Integer | N | 녹음 길이(초). 없을 시 `null` | `30` |
| data.records[].createdAt | String (ISO-8601) | Y | 게시 시각 | `"2026-05-14T16:19:02+09:00"` |

> 좋아요(`♥ 24`) / 댓글(`💬 8`) 카운트는 **클라 하드코딩** 으로 노출 — 서버 응답에 포함하지 않는다.
> 카드 본문 텍스트(디자인 001 의 1~2줄) 도 **클라 하드코딩** (`"안녕하세요"`) 으로 노출 — `title` 은 사용자 입력 제목 그대로.

### Success Response Example

**200 OK**

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공했습니다.",
  "data": {
    "records": [
      {
        "recordId": 42,
        "author": { "name": "박순자", "age": 68 },
        "title": "상추 모종 심을 땐 아침에만 물을 줘야 해요",
        "photoUrl": "https://cdn.example.com/records/42/lettuce.jpg",
        "voiceDurationSeconds": 30, "createdAt": "2026-05-14T16:19:02+09:00"
      }
    ]
  }
}
```

### Error Response Example

**500 Internal Server Error**

```json
{
  "success": false,
  "status": 500,
  "message": "서버 내부 오류가 발생했습니다.",
  "code": "COM_500_001",
  "meta": {
    "path": "/api/v1/home/records",
    "timestamp": "2026-05-14T16:19:02+09:00"
  }
}
```

---

## 노트

- 카드 탭 → 상세 페이지 이동은 **MVP 미구현** (디자인 노트). 따라서 본 명세는 카드 목록까지만 정의한다.
- **좋아요·댓글 카운트** 는 클라 하드코딩 — 서버 응답·DB 모두 보관하지 않음. 좋아요 토글 API 도 두지 않음.
- **본문 텍스트** (디자인 001 의 카드 본문 1~2줄) 는 클라 하드코딩 `"안녕하세요"`.
- 페이지네이션 미적용 — 데이터 증가 시 cursor 기반으로 확장 예정.
