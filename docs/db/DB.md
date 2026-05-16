# DB 테이블 명세

| 항목 | 내용 |
|------|------|
| 서비스 | 노화우 (老하우) |
| DBMS | MySQL 8.0 (운영) / H2 (테스트) |
| 공통 컬럼 | `created_at`, `updated_at` — `BaseEntity` 상속 |
| 작성일 | 2026-05-17 |

> MVP 기준 2 테이블 — `user` / `record`.
> 좋아요·댓글 수는 DB 에 보관하지 않는다 — 클라 하드코딩으로 노출.

---

## 1. 사용자

서비스 사용자(부모/또래) 1인을 표현한다. 이름·나이를 카드에 노출하기 위해 보관.

**테이블명:** `user`

| Key | Name | Type | Constraint | Description | Example |
|-----|------|------|------------|-------------|---------|
| PK  | id | BIGINT | NOT NULL, AUTO_INCREMENT | 사용자 ID | `1` |
|     | name | VARCHAR(50) | NOT NULL | 이름 (카드에 `{name}님` 으로 노출) | `박순자` |
|     | age | INT | NOT NULL | 나이 (카드에 `· {age}세` 로 노출) | `68` |
|     | profile_image_url | VARCHAR(255) | NULL | 프로필 이미지 URL (HOME 카드 작성자 썸네일). 기존 행과의 호환을 위해 NULL 허용 — 더미·신규 데이터는 항상 채워짐 | `/dummy/profile_01.png` |
|     | created_at | TIMESTAMP | NOT NULL | 생성 시각 | `2026-05-14 16:19:00` |
|     | updated_at | TIMESTAMP | NOT NULL | 수정 시각 | `2026-05-14 16:19:00` |

**Example Row**

| id | name | age | profile_image_url | created_at | updated_at |
|----|------|-----|-------------------|------------|------------|
| 1 | 박순자 | 68 | /dummy/profile_01.png | 2026-05-14 16:19:00 | 2026-05-14 16:19:00 |

---

## 2. 노하우 기록

부모 사용자가 등록한 노하우 카드 1건.

**테이블명:** `record`

| Key | Name | Type | Constraint | Description | Example |
|-----|------|------|------------|-------------|---------|
| PK  | id | BIGINT | NOT NULL, AUTO_INCREMENT | 카드 ID | `1` |
|     | user_id | BIGINT | NOT NULL | 작성자 (논리적 FK → `user.id`, DB 제약 미적용) | `1` |
|     | title | VARCHAR(255) | NOT NULL | 사용자가 직접 입력하는 기록 제목 (디자인 002 의 텍스트 필드 입력값. 음성 녹음과 무관) | `상추 모종 심기` |
|     | photo_url | VARCHAR(500) | NULL | 첨부 사진 URL (미첨부 시 NULL). 더미는 `/dummy/p1.png` 등, 클라 업로드는 `/uploads/{uuid}.jpg` 형태 | `/dummy/p1.png` |
|     | voice_duration_seconds | INT | NULL | 녹음 길이(초). HOME 카드 우상단 칩 노출 | `30` |
|     | is_shared | BOOLEAN | NOT NULL | 또래 게시판 공유 여부 | `false` |
|     | location | VARCHAR(255) | NOT NULL | 안드로 클라가 EXIF GPS 를 변환해 보내는 한글 주소 문자열. DETAIL 위치 칩 노출 | `서울시 마포구 망원동` |
|     | recorded_at | TIMESTAMP | NOT NULL | EXIF DateTimeOriginal — 사진 촬영 시각 (KST). DETAIL 타이틀 / ARCHIVE 캘린더 일자 매핑 기준 | `2026-05-14 16:19:02` |
|     | created_at | TIMESTAMP | NOT NULL | 서버 저장 시각 | `2026-05-14 16:19:02` |
|     | updated_at | TIMESTAMP | NOT NULL | 수정 시각 | `2026-05-14 16:19:02` |

**Index**

| Name | Columns | Purpose |
|------|---------|---------|
| `idx_record_is_shared_created_at` | (`is_shared`, `created_at` DESC) | HOME 또래 게시판 최신순 조회 |
| `idx_record_user_id_created_at` | (`user_id`, `created_at` DESC) | ARCHIVE 본인 월별 조회 |

**Example Row**

| id | user_id | title | photo_url | voice_duration_seconds | is_shared | location | recorded_at | created_at |
|----|---------|-------|-----------|------------------------|-----------|----------|-------------|------------|
| 1 | 1 | 상추 모종 심기 | /dummy/p1.png | 30 | true | 서울시 마포구 망원동 | 2026-05-14 16:19:02 | 2026-05-14 16:19:02 |

---

## ERD 관계

```
[user] 1 ─── N [record]   (작성자)
```

---

## MVP 운영 노트

- **좋아요·댓글 수 미보관** — 컬럼 자체 없음. HOME 카드의 `♥ 24` / `💬 8` 은 클라 하드코딩으로 표시. 서버 API 도 별도 두지 않음.
- **음성 녹음 / STT** — 클라가 녹음 UX 수행 후 STT 결과를 받은 척 `"안녕하세요"` 로 하드코딩 보관. 서버는 본문을 수신·저장하지 않으며, 길이(`voice_duration_seconds`) 만 받아 HOME 카드 우상단 칩에 노출.
- **본문 컬럼 없음** — 위 이유로 DB 에 본문 텍스트 컬럼 자체를 두지 않는다. 화면의 본문 영역은 클라가 들고 있는 하드코딩 값(`"안녕하세요"`) 을 그대로 노출.
- **위치 / 촬영 시각** — 안드로 클라가 사진 EXIF (GPS, DateTimeOriginal) 에서 추출해 `location` (한글 주소 문자열), `recorded_at` (ISO-8601) 로 등록 시 함께 보낸다. DETAIL 의 위치 칩 / 타이틀 일자가 이 값들 기준으로 노출됨.
- **ddl-auto** — `application.yaml` 의 `ddl-auto: update` 환경에서 엔티티 추가 시 컬럼·테이블이 자동 반영됨 (운영 영향 큰 변경은 사전 공유).
- **공통 컬럼** — `created_at`, `updated_at` 은 `global/persistence/BaseEntity` 상속으로 자동 관리.
