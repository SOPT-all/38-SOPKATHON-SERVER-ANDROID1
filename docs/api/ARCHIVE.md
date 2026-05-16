# ARCHIVE 나의 노하우집 API 명세

---

## API 1. 월별 캘린더 + 통계 조회

본인이 등록한 노하우 카드의 월별 요약을 한 번에 조회한다.
화면 상단의 통계 카드(이번 달 기록 수 / 공유한 노하우 수) 와 캘린더 그리드(일자별 썸네일) 를 한 응답으로 채운다.

**GET** `/api/v1/me/records/calendar`

### Query Parameter

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| year | Integer | N | 조회 연도. 미전달 시 오늘 날짜 기준 연도 | `2026` |
| month | Integer | N | 조회 월 (1~12). 미전달 시 오늘 날짜 기준 월 | `5` |

### Response Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| success | Boolean | Y | 요청 성공 여부 | `true` |
| status | Integer | Y | HTTP 상태 코드 | `200` |
| message | String | Y | 응답 메시지 | `"요청이 성공했습니다."` |
| data | Object | Y | 응답 데이터 | - |
| data.year | Integer | Y | 응답 연도 | `2026` |
| data.month | Integer | Y | 응답 월 | `5` |
| data.recordCount | Integer | Y | 해당 월 기록 총 개수 (통계 카드 좌측) | `18` |
| data.sharedCount | Integer | Y | 해당 월 또래 게시판 공유(isShared=true) 개수 (통계 카드 우측) | `7` |
| data.days | Array | Y | 해당 월 일자별 정보 (기록 있는 날짜만) | - |
| data.days[].day | Integer | Y | 일(1~31) | `14` |
| data.days[].recordId | Long | Y | 해당 일의 대표 카드 ID (다수 시 최신) | `1` |
| data.days[].thumbnailUrl | String | N | 셀 배경 썸네일 (사진 없을 시 `null`) | `"https://.../lettuce_thumb.jpg"` |

> 일자 매핑은 카드의 `createdAt` 기준 (사진 EXIF 시각은 사용 안 함).
> 한 날짜에 다수 기록이 있을 경우의 셀 처리는 디자인 미확정 — 임시로 최신 1건만 응답. 추후 확장 가능.
> 셀 색상은 클라가 썸네일에서 자체 산출 (서버 미제공).

### Success Response Example

**200 OK**

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공했습니다.",
  "data": {
    "year": 2026,
    "month": 5,
    "recordCount": 18,
    "sharedCount": 7,
    "days": [
      {
        "day": 14,
        "recordId": 1,
        "thumbnailUrl": "https://cdn.example.com/records/1/lettuce_thumb.jpg"
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
    "path": "/api/v1/me/records/calendar",
    "timestamp": "2026-05-14T16:19:02+09:00"
  }
}
```

---
