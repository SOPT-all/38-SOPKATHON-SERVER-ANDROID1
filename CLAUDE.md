# CLAUDE.md

SOPT 38기 솝커톤 안드로이드1팀 서버 레포지토리.

## 기술 스택

- Java 21 / Spring Boot 3.5.14
- Spring Data JPA, Spring Security, Spring Validation, Actuator
- MySQL 8.0 (운영) / H2 (테스트)
- Gradle (Kotlin DSL)
- Docker Compose + Caddy (리버스 프록시 / 자동 HTTPS)
- 배포: GitHub Actions → EC2 SSH

## 디렉터리 구조

```
src/main/java/org/sopt/android1   애플리케이션 코드 (base package)
src/main/resources                application.yaml 등
docs/api                          화면별 API 명세 템플릿
docs/db                           DB 테이블 명세 / ERD
docs/deploy                       배포 가이드
docs/design                       디자인 산출물
docs/plan                         IA / 시나리오 / 페이지 기획
```

## 코딩 컨벤션

코드 작성 시 반드시 아래 규칙을 따른다.

### 네이밍

| 대상 | 형식 | 예시 |
|------|------|------|
| 클래스 / 인터페이스 / 열거형 | PascalCase | `ContestService`, `ErrorCode` |
| 메서드 / 변수 | camelCase | `getContestInfo`, `remainingSeconds` |
| 상수 (`static final`) | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | 소문자, 단수형 | `controller`, `dto`, `entity` |
| DTO (Request) | `{Domain}Request` | `ContestEnterRequest` |
| DTO (Response) | `{Domain}Response` | `ContestEnterResponse` |
| 엔티티 | `{Domain}Entity` | `ContestEntity` |
| 예외 | `{Domain}Exception` | `ContestException` |
| Repository | `{Domain}Repository` | `ContestRepository` |
| 테스트 클래스 | `{ClassName}Test` | `ContestServiceTest` |

### 클래스 내 멤버 선언 순서

1. 상수 (`static final`)
2. `static` 변수
3. 인스턴스 변수
4. 생성자
5. `public` 메서드 (getter / setter는 `public` 메서드 전부 작성 후 마지막에)
6. `protected` 메서드
7. package-private 메서드
8. `private` 메서드
9. nested 클래스 / 인터페이스

### 패키지 구조

> Base Package: `org.sopt.android1`

```
src/main/java/org/sopt/android1
├── global
│   ├── config
│   ├── persistence
│   │   └── BaseEntity
│   ├── exception
│   │   ├── GlobalExceptionHandler
│   │   ├── ErrorCode
│   │   └── ErrorResponse
│   └── util
│
├── domain
│   └── {domain}
│       ├── controller
│       │   └── {Domain}Controller
│       ├── service
│       │   └── {Domain}Service
│       ├── repository
│       │   └── {Domain}Repository
│       ├── entity
│       │   └── {Domain}Entity
│       ├── dto
│       │   ├── request
│       │   │   └── {Domain}Request
│       │   └── response
│       │       └── {Domain}Response
│       └── exception
│           └── {Domain}Exception
│
└── Application
```

| 패키지 | 역할 |
|--------|------|
| `global/config` | Spring 설정 클래스 (Security, CORS 등) |
| `global/persistence` | 공통 엔티티 (`BaseEntity` — createdAt, updatedAt) |
| `global/exception` | 전역 예외 처리 (`GlobalExceptionHandler`), 에러코드 (`ErrorCode`), 에러 응답 포맷 (`ErrorResponse`) |
| `global/util` | 공통 유틸리티 클래스 |
| `domain/{domain}/controller` | REST API 진입점 |
| `domain/{domain}/service` | 비즈니스 로직 |
| `domain/{domain}/repository` | 데이터 접근 (JPA Repository) |
| `domain/{domain}/entity` | JPA 엔티티 |
| `domain/{domain}/dto/request` | 요청 DTO |
| `domain/{domain}/dto/response` | 응답 DTO |
| `domain/{domain}/exception` | 도메인별 커스텀 예외 |

## API 응답 포맷

`docs/api/API.md` 참고. 성공/실패 공통 응답:

```json
{ "success": true, "status": 200, "message": "...", "data": {} }
```

실패 시 `code`, `meta.path`, `meta.timestamp` 포함.

## 자주 쓰는 명령어

```bash
./gradlew bootRun                # 로컬 실행
./gradlew test                   # 테스트
./gradlew clean bootJar          # 빌드
docker compose up -d --build     # MySQL + 앱 + Caddy 기동
```

## 환경 변수

`.env.example` 참고. 로컬은 `application.yaml`의 기본값으로 동작하지만, Docker / 운영에서는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `DOMAIN` 필요.

## 배포

- `main` 브랜치 푸시 시 GitHub Actions가 EC2에 SSH 접속해 자동 배포 (`.github/workflows/deploy.yml`).
- 상세 절차: `docs/deploy/DEPLOY.md`.

## 작업 방식

### Plan 단계
1. 항상 Plan Mode로 시작한다.
2. 작업을 기능 단위로 나누고 전체 작업 범위를 정의한다.
3. 작업 범위를 간단히 브리핑한 뒤 사용자 확인을 받는다.
4. 승인 전에는 구현을 시작하지 않는다.

### 작업 진행
5. 기능 단위로 작업한다.
6. 각 작업 완료 후 반드시 멈춘다.
7. 다음 단계 진행 전 사용자 확인을 받는다.

### 커밋 규칙
8. 커밋 생성 전, 변경 요약을 먼저 제시하고 승인을 받는다.
9. 승인 후에만 커밋한다.
10. 커밋 메시지에 `Co-Authored-By` 라인을 포함하지 않는다.

### 작업 플로우
1. `gh issue create` 로 이슈를 생성한다.
2. 이슈 번호를 받아 브랜치를 생성한다 (예: `chore/#1/global`).
3. 브랜치에서 작업하고 커밋 → 푸시한다.
4. `gh pr create` 로 PR을 생성한다. **PR 제목은 이슈 제목과 동일하게** 작성한다 (이슈 번호 등 부가 정보 추가 금지).
5. PR 머지는 사용자가 수동으로 한다.
6. 머지 완료 승인을 받은 뒤, `main` 체크아웃 → `git pull` 로 최신화한다.

## 작업 시 유의사항

- `ddl-auto: update`로 동작 중 — 엔티티 변경 시 컬럼/테이블이 자동 반영되므로, 운영 영향 큰 변경은 사전 공유.
- 보안 설정은 현재 모든 요청 `permitAll` + CORS 전체 허용. 인증 도입 시 `SecurityConfig` 수정 필요.
- 새 도메인 추가 시 `docs/db/DB.md`, `docs/api/API.md`의 명세도 함께 갱신.
