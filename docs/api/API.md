# [SCREEN_ID] [화면명] API 명세

---

## API N. [API 이름]

[API에 대한 한 줄 설명]

**[HTTP_METHOD]** `[엔드포인트 경로]`

### Path Parameter

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
|      |      |          |             |         |

### Query Parameter

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
|      |      |          |             |         |

### Request Header

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
|      |      |          |             |         |

### Request Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
|      |      |          |             |         |

### Request Example

```
[HTTP_METHOD] [엔드포인트 경로]
```

### Response Body

| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| success | Boolean | Y | 요청 성공 여부 | `true` |
| status | Integer | Y | HTTP 상태 코드 | `200` |
| message | String | Y | 응답 메시지 | `"요청이 성공했습니다."` |
| data | Object | Y | 응답 데이터 | - |

### Success Response Example

**200 OK**

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

### Error Response Example

**[ERROR_STATUS] [ERROR_NAME]**

```json
{
  "success": false,
  "status": 0,
  "message": "",
  "code": "",
  "meta": {
    "path": "",
    "timestamp": 0
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
    "path": "",
    "timestamp": 0
  }
}
```

---
