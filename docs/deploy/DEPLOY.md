# 운영 배포 가이드

`android1` (38기 솝커톤 안드로이드1팀 서버) 백엔드를 EC2에 배포하는 절차를 정리한다.
로컬 개발 환경 셋업은 `README.md`를 참고한다.

> **배포 정책 한 줄 요약**
> - **로컬 / 운영 모두 `docker-compose.yml` 한 벌**로 동작한다 (MySQL 8 + Spring Boot + Caddy).
> - **MySQL은 EC2 안 도커 컨테이너**에서 돈다 (RDS 사용 안 함).
> - **HTTPS는 Caddy 컨테이너**가 자동 발급/갱신한다 (Let's Encrypt).
> - **자동 배포는 GitHub Actions**가 `main` push 시 EC2에 ssh 접속해 `git pull && docker compose up -d --build` 실행.

---

## 현재 배포 리소스

| 분류 | 항목 | 값 |
|------|------|-----|
| 도메인 | 운영 도메인 | `sopkathon.o-r.kr` (내도메인.한국 무료 서브도메인) |
| 리전 | 운영 리전 | `ap-northeast-2` (서울) |
| EC2 | 인스턴스 사용자 | `ubuntu` (Ubuntu LTS 가정) |
| EC2 | 앱 디렉토리 | GitHub Secrets `EC2_APP_DIR` 에 지정 (예: `/home/ubuntu/android1`) |
| EC2 | 퍼블릭 IPv4 | GitHub Secrets `EC2_HOST` |
| 컨테이너 | DB | `mysql:8.0` (named volume `mysql_data`) |
| 컨테이너 | 앱 | `Dockerfile`로 빌드 (`eclipse-temurin:21`, Spring Boot 3.5) |
| 컨테이너 | 리버스 프록시 | `caddy:2-alpine` (`./Caddyfile` 마운트) |
| 포트 | 외부 노출 | 80 / 443 (Caddy만) — 앱 8080은 docker 네트워크 내부 |
| CI | 자동 배포 | `.github/workflows/deploy.yml` (appleboy/ssh-action) |

신규 리소스/시크릿이 추가되면 이 표를 즉시 갱신한다.

---

## 아키텍처

```
[ 클라이언트 ]
      │
      │  HTTPS (443) / HTTP (80)
      ▼
┌──────────────────────────────────── EC2 ────────────────────────────────────┐
│                                                                              │
│  [ caddy:2-alpine ]                                                          │
│      │  reverse_proxy app:8080  (docker network)                             │
│      ▼                                                                       │
│  [ app  (Dockerfile, Spring Boot 3.5 / Java 21) ]                            │
│      │  JDBC (mysql:3306)                                                    │
│      ▼                                                                       │
│  [ mysql:8.0  (volume mysql_data) ]                                          │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

- Caddy만 80/443을 호스트로 노출. 앱·MySQL은 `expose` 만 해서 외부에서 직접 접근 불가.
- 앱은 호스트네임 `mysql` / `app` 으로 컨테이너 간 통신 (compose가 만든 default 네트워크).
- 인증서는 Caddy가 Let's Encrypt에서 자동 발급. `caddy_data` 볼륨에 저장되어 컨테이너 재시작에도 유지.

---

## 사전 준비물

| 항목 | 비고 |
|------|------|
| Ubuntu LTS EC2 인스턴스 | 80/443/22 인바운드 개방, 퍼블릭 IPv4 또는 EIP |
| 도메인 | `sopkathon.o-r.kr` (현재). A 레코드가 EC2 IP를 가리켜야 Caddy가 인증서 발급 가능 |
| EC2에 Docker + Docker Compose plugin 설치 | §2 참고 |
| GitHub 저장소 secrets 4개 | `EC2_HOST`, `EC2_USERNAME`, `EC2_SSH_KEY`, `EC2_APP_DIR` (§4) |
| 운영 `.env` 1개 | EC2의 `EC2_APP_DIR` 안에 직접 작성, git 미포함 (§3) |

---

## 1. DNS

운영 도메인은 `sopkathon.o-r.kr`. 내도메인.한국에서 해당 서브도메인을 등록한 뒤 DNS 관리 화면에서:

| 칸 | 입력값 |
|----|--------|
| 타입 | A |
| 호스트 | (비움) — apex 자체 매핑 |
| IP | EC2 퍼블릭 IPv4 (EIP 권장) |

전파 확인:

```bash
dig @8.8.8.8 sopkathon.o-r.kr +short
```

→ EC2 IP가 한 줄 출력되면 완료. 보통 1~10분.

> ⚠️ DNS가 정상 전파되기 전에 컨테이너를 띄우면 Caddy의 ACME HTTP-01 챌린지가 실패해서 인증서 발급이 안 된다. **DNS 먼저, 그다음 docker compose**.

---

## 2. EC2 서버 초기 설정

ssh 접속 후 1회만 수행.

### 2.1 Docker + Compose plugin 설치

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# ubuntu 계정으로 sudo 없이 docker 사용
sudo usermod -aG docker ubuntu
# 로그아웃 후 재로그인해야 그룹 반영됨

docker --version
docker compose version
```

### 2.2 방화벽 / 보안그룹

EC2 보안그룹 인바운드:

| 포트 | 소스 | 용도 |
|------|------|------|
| 22  | `0.0.0.0/0` (또는 팀원 IP 한정) | ssh (GitHub Actions도 이 포트 사용) |
| 80  | `0.0.0.0/0` | Caddy HTTP + Let's Encrypt HTTP-01 챌린지 |
| 443 | `0.0.0.0/0` (TCP/UDP 둘 다) | Caddy HTTPS (UDP는 HTTP/3용) |

> 8080은 절대 외부에 열지 않는다. `docker-compose.yml`의 `app` 서비스는 `expose: ["8080"]`로만 선언되어 호스트로 매핑되지 않는다.

### 2.3 코드 클론

GitHub Actions가 사용할 `EC2_APP_DIR` 위치에 미리 클론해둔다.

```bash
cd ~
git clone <repo URL> android1
cd android1
```

> Actions 워크플로는 `cd $EC2_APP_DIR && git pull` 만 실행한다. 첫 클론은 사람이 한 번 해줘야 한다.

### 2.4 ssh 키 (GitHub Actions용)

Actions가 EC2에 접속할 키는 ED25519 권장.

```bash
# 로컬에서 키 페어 생성 (한 번만)
ssh-keygen -t ed25519 -f ~/.ssh/sopkathon_deploy -C "github-actions"

# EC2의 ubuntu 사용자 authorized_keys에 공개키 추가
cat ~/.ssh/sopkathon_deploy.pub | ssh ubuntu@<EC2_IP> 'cat >> ~/.ssh/authorized_keys'
```

비공개키(`sopkathon_deploy`)는 GitHub Secrets `EC2_SSH_KEY`에 통째로 붙여넣는다 (§4).

---

## 3. 운영 `.env` 작성

`.env`는 git에 포함되지 않는다 (`.gitignore`에서 제외). EC2의 `EC2_APP_DIR` 루트에 직접 작성한다.

`.env.example` 형식 그대로:

```env
MYSQL_ROOT_PASSWORD=<강력한_비밀번호>
MYSQL_DATABASE=android1
DOMAIN=sopkathon.o-r.kr
```

설명:

| 변수 | 쓰이는 곳 | 비고 |
|------|----------|------|
| `MYSQL_ROOT_PASSWORD` | `mysql` 컨테이너 초기화 + `app` 컨테이너의 `DB_PASSWORD` | 최초 부팅 시 1회 사용. **변경하려면 `mysql_data` 볼륨을 지우고 재초기화해야 함** |
| `MYSQL_DATABASE` | `mysql` 초기 DB 이름 + `app` JDBC URL의 schema | 현재 `android1` 고정 |
| `DOMAIN` | Caddy가 `{$DOMAIN}` 자리에 치환 → 해당 도메인으로 인증서 발급 | DNS가 이 도메인을 EC2로 가리켜야 함 |

권한 잠금:

```bash
chmod 600 .env
```

> ⚠️ `.env` 값에 공백이나 `&`가 포함되면 큰따옴표로 감싼다. Docker Compose는 따옴표를 strip 해서 컨테이너 환경변수로 전달한다.

> ⚠️ `MYSQL_ROOT_PASSWORD`는 MySQL 컨테이너가 **빈 데이터 디렉토리(=`mysql_data` 볼륨이 없는 상태)에서 부팅될 때만 적용**된다. 이미 볼륨이 있는 상태에서 값만 바꾸면 앱은 새 비밀번호로 접속하려 하고 MySQL은 옛 비밀번호로 동작해서 인증 실패가 난다. 비밀번호 교체는 §6.3 참조.

---

## 4. GitHub Actions Secrets

저장소 → Settings → Secrets and variables → Actions → New repository secret 으로 4개 등록:

| Secret 이름 | 값 |
|-------------|-----|
| `EC2_HOST` | EC2 퍼블릭 IPv4 (예: `13.124.x.x`) 또는 도메인 |
| `EC2_USERNAME` | `ubuntu` |
| `EC2_SSH_KEY` | §2.4에서 만든 비공개키(`sopkathon_deploy`) 전체 — `-----BEGIN OPENSSH PRIVATE KEY-----` 부터 끝줄까지 |
| `EC2_APP_DIR` | EC2에서 코드를 클론한 절대경로 (예: `/home/ubuntu/android1`) |

`.github/workflows/deploy.yml` 가 위 4개를 사용한다:

```yaml
- uses: appleboy/ssh-action@v1.2.0
  with:
    host: ${{ secrets.EC2_HOST }}
    username: ${{ secrets.EC2_USERNAME }}
    key: ${{ secrets.EC2_SSH_KEY }}
    script: |
      set -e
      cd ${{ secrets.EC2_APP_DIR }}
      git pull
      docker compose up -d --build
      docker image prune -f
```

> Actions는 `main` 브랜치 push 시 자동 실행, 또는 Actions 탭에서 `Run workflow` 로 수동 실행 가능 (`workflow_dispatch`).

---

## 5. 첫 배포 (수동)

자동 배포 워크플로가 잘 동작하려면 **첫 부팅을 사람이 한 번 확인하는 게 안전**하다. EC2 ssh 후:

```bash
cd ~/android1
git pull                              # main 최신화
docker compose up -d --build          # 빌드 + 백그라운드 기동

docker compose ps                     # 세 서비스 모두 'running'/'healthy' 인지
docker compose logs -f caddy          # ACME 인증서 발급 로그 (Ctrl+C 로 빠져나오기)
docker compose logs -f app            # Spring 부팅 로그
```

Caddy 로그에서 다음과 같은 줄이 보이면 인증서 발급 성공:

```
certificate obtained successfully
serving initial configuration
```

검증:

```bash
# 호스트에서
curl -sI https://sopkathon.o-r.kr/actuator/health | head -3
# HTTP/2 200

# 외부에서 (로컬 머신)
curl https://sopkathon.o-r.kr/actuator/health
# {"status":"UP"}
```

> `application.yaml` 의 `management.endpoints.web.exposure.include: health` 덕분에 `/actuator/health` 만 외부 노출. 그 외 actuator 엔드포인트는 닫혀있다.

---

## 6. 운영 작업

### 6.1 자동 배포 흐름 (`main` push)

1. 개발자가 `main`에 push (또는 PR merge).
2. GitHub Actions가 EC2에 ssh.
3. `git pull` → `docker compose up -d --build` → `docker image prune -f`.
4. 변경된 서비스만 재생성. 그렇지 않은 컨테이너(mysql 등)는 그대로 유지.

> 빌드 실패 시 EC2의 기존 컨테이너는 그대로 계속 돈다 (compose가 새 컨테이너 생성 단계에서 멈춤). 운영 다운타임 거의 없음.

### 6.2 수동 재배포 / 트러블슈팅

```bash
# EC2 ssh 후
cd ~/android1

# 컨테이너 상태
docker compose ps
docker compose logs --tail 200 app
docker compose logs --tail 200 caddy
docker compose logs --tail 200 mysql

# 앱만 재빌드/재기동
docker compose up -d --build app

# 전부 재기동 (이미지 캐시 활용)
docker compose restart

# 완전 정리 + 재기동 (이미지·캐시는 유지, 컨테이너만 새로)
docker compose down
docker compose up -d --build
```

### 6.3 MySQL 비밀번호 / 초기 DB 변경

`MYSQL_ROOT_PASSWORD` 와 `MYSQL_DATABASE` 는 **빈 볼륨 초기화 시점에만 적용**된다. 운영 중 값만 바꾸면 인증 실패가 난다. 변경 절차:

```bash
# EC2 ssh 후
cd ~/android1

# 1) 데이터 백업
docker compose exec mysql \
  mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 \
  --databases android1 > backup_$(date +%F).sql

# 2) 컨테이너 + 볼륨 제거 (⚠️ 데이터 전부 사라짐)
docker compose down
docker volume rm android1_mysql_data    # 프로젝트명_볼륨명. `docker volume ls` 로 정확한 이름 확인

# 3) .env 의 비밀번호/DB명 수정 후 재기동
vim .env
docker compose up -d

# 4) 백업 복원
cat backup_*.sql | docker compose exec -T mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4
```

> 백업/복원 시 `--default-character-set=utf8mb4` 빠뜨리지 말 것. 누락하면 한글 문자열이 `latin1` → `utf8mb4` 더블 인코딩되어 mojibake 상태로 저장된다.

### 6.4 DB 직접 조회 / 수정

자세한 확인 절차는 §7 (DB에 데이터 잘 들어가는지 수동으로 확인하기) 참고. 빠른 연결만 필요하면:

```bash
# EC2 ssh 후
docker compose exec mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 android1
```

> 운영 DB에서 다중 행 수정·삭제는 반드시 `START TRANSACTION;` ~ `COMMIT;` 으로 감싸고, 실행 전에 `SELECT` 로 영향 행을 먼저 확인한다.

### 6.5 도메인 변경

`.env` 의 `DOMAIN=` 만 새 도메인으로 바꾸고 `docker compose up -d caddy` 로 Caddy만 재기동. DNS A 레코드가 EC2 IP를 가리키고 있어야 자동 재발급.

### 6.6 디스크 / 이미지 정리

```bash
# 사용 안 하는 이미지/네트워크/캐시 정리 (안전)
docker system prune -f

# 사용 안 하는 볼륨까지 (⚠️ 정지된 컨테이너 데이터 날아갈 수 있음)
docker system prune -f --volumes
```

자동 배포 워크플로 마지막 단계에서 `docker image prune -f` 가 매번 돌고 있어 보통 수동 청소는 거의 필요 없다.

---

## 7. DB에 데이터 잘 들어가는지 수동으로 확인하기

API를 호출했는데 응답은 200인데 진짜로 DB 행이 만들어졌는지 눈으로 확인하고 싶을 때 쓰는 절차다. **EC2에 ssh로 들어가서, MySQL 컨테이너에 한 줄 명령으로 접속해 SQL을 친다.** 처음 보는 사람도 그대로 따라할 수 있게 단계별로 적었다.

### 7.1 MySQL에 접속하기

EC2에 ssh로 접속한 다음 (`ssh ubuntu@<EC2_IP>`), 프로젝트 디렉토리로 이동:

```bash
cd ~/android1
```

MySQL 컨테이너에 mysql CLI로 접속:

```bash
docker compose exec mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 android1
```

분해해서 읽으면:

| 조각 | 의미 |
|------|------|
| `docker compose exec mysql` | compose가 띄운 `mysql` 서비스 컨테이너 안에서 명령 실행 |
| `mysql -uroot -p"..."` | 컨테이너 안의 mysql CLI 실행, root 계정으로 로그인 |
| `--default-character-set=utf8mb4` | 한글 안 깨지게 |
| `android1` | 접속하면서 바로 `android1` 데이터베이스 선택 |

> ⚠️ `$MYSQL_ROOT_PASSWORD` 가 빈 문자열로 나오면 셸에 환경변수가 안 잡힌 것이다. `set -a; source .env; set +a` 한 번 실행하고 다시 시도. (또는 비밀번호를 직접 `-p'실제비밀번호'` 로 박아도 됨.)

성공하면 프롬프트가 이렇게 바뀐다:

```
mysql>
```

여기서부터는 SQL을 친다. 한 줄 끝에 **세미콜론(`;`)** 을 꼭 붙여야 실행된다.

종료는:

```sql
EXIT;
```

또는 `\q` + Enter.

### 7.2 어떤 테이블이 있는지 둘러보기

```sql
SHOW DATABASES;
```

→ `android1` 이 목록에 있어야 한다.

```sql
USE android1;
SHOW TABLES;
```

→ 앱에서 정의한 JPA 엔티티(`@Entity`) 들이 테이블로 보여야 한다.

> **테이블이 하나도 안 보일 때**: 현재 `application.yaml` 의 `spring.jpa.hibernate.ddl-auto: update` 설정 때문에 **앱이 부팅하면서 엔티티를 보고 테이블을 만든다**. 다음 중 하나를 확인:
> - 엔티티(`@Entity`) 클래스를 아직 안 만들었으면 당연히 테이블도 없음.
> - 엔티티는 있는데 테이블이 없으면 앱 부팅 중 에러일 가능성. `docker compose logs app | grep -i error` 확인.
> - 엔티티를 새로 추가한 직후라면 `docker compose restart app` 으로 앱 한 번 재기동.

### 7.3 테이블 구조(컬럼) 보기

```sql
DESC user;
```

(`user` 자리에 보고 싶은 테이블명) 출력 예:

```
+----------+--------------+------+-----+---------+----------------+
| Field    | Type         | Null | Key | Default | Extra          |
+----------+--------------+------+-----+---------+----------------+
| id       | bigint       | NO   | PRI | NULL    | auto_increment |
| nickname | varchar(255) | YES  |     | NULL    |                |
+----------+--------------+------+-----+---------+----------------+
```

각 컬럼의 타입·NULL 허용 여부·기본값을 한눈에 볼 수 있다.

### 7.4 데이터 들어왔는지 확인 — 기본 SELECT

```sql
SELECT * FROM user;
```

행이 너무 많으면 화면이 도배되므로 처음에는 LIMIT를 붙이는 습관을 들인다:

```sql
SELECT * FROM user LIMIT 10;
```

### 7.5 "지금 막 들어간 거" 보기 — 최근 N개

PK가 `auto_increment` 면 가장 큰 id가 가장 최근에 들어간 행이다.

```sql
SELECT * FROM user ORDER BY id DESC LIMIT 10;
```

생성 시각 컬럼(`created_at`, `createdAt` 등)이 있으면 그걸로 정렬:

```sql
SELECT * FROM user ORDER BY created_at DESC LIMIT 10;
```

### 7.6 몇 개가 들어있는지 — COUNT

```sql
SELECT COUNT(*) FROM user;
```

API 호출 전/후로 한 번씩 찍어보면 진짜로 1행이 늘었는지 즉시 확인된다.

### 7.7 조건으로 좁혀서 보기 — WHERE

```sql
SELECT * FROM user WHERE nickname = '홍길동';
SELECT * FROM user WHERE id = 1;
SELECT * FROM user WHERE nickname LIKE '홍%';        -- '홍'으로 시작
SELECT * FROM user WHERE created_at >= '2026-05-16';  -- 오늘 이후 생성
```

문자열은 작은따옴표(`'...'`)로 감싼다.

### 7.8 한글이 깨지지 않고 들어갔는지 확인

`SELECT` 결과로 한글이 정상적으로 보이면 일단 OK. 의심스러우면 바이트 레벨로 확인:

```sql
SELECT id, nickname, HEX(nickname) FROM user WHERE id = 1;
```

UTF-8에서 한글 한 글자는 **3바이트(6자리 hex)** 다. 예시:

| 글자 | 정상 hex |
|------|---------|
| 가   | `EAB080` |
| 안   | `EC9588` |
| 영   | `EC9881` |
| 홍   | `ED998D` |

예를 들어 `홍길동` 이 들어갔다면 `HEX(nickname)` 결과가 `ED998DEAB8B8EB8F99` 처럼 9바이트(3글자 × 3바이트)여야 한다.

만약 한 글자당 6바이트(예: `ECED9989EDB58D`)처럼 길게 나오면 **mojibake 더블 인코딩** 된 상태 — `latin1` 로 한 번, `utf8mb4` 로 또 한 번 인코딩된 것이다. 입력 경로(JDBC URL `characterEncoding=UTF-8`, mysql CLI `--default-character-set=utf8mb4`) 어딘가에서 인코딩이 빠진 것이니 그쪽을 점검한다.

### 7.9 시간대(timezone) 확인

MySQL 컨테이너는 `docker-compose.yml` 에 `TZ: Asia/Seoul` 로 설정돼 있다. 확인:

```sql
SELECT NOW(), @@global.time_zone, @@session.time_zone;
```

→ `NOW()` 가 한국 시각으로 보이면 정상.

특정 행의 timestamp 컬럼도 같이 비교:

```sql
SELECT id, created_at, NOW(), TIMESTAMPDIFF(SECOND, created_at, NOW()) AS sec_ago
FROM user
ORDER BY id DESC LIMIT 5;
```

방금 만든 행의 `sec_ago` 가 한 자리수면 정상.

### 7.10 흔히 같이 쓰는 디버깅 한 줄들

```sql
-- 어떤 테이블이 가장 큰지 (행 수 추정치, 즉시 응답)
SELECT TABLE_NAME, TABLE_ROWS
FROM information_schema.tables
WHERE table_schema = 'android1'
ORDER BY TABLE_ROWS DESC;

-- 특정 컬럼이 NULL 인 행만
SELECT * FROM user WHERE nickname IS NULL;

-- 중복 닉네임 찾기
SELECT nickname, COUNT(*) AS cnt
FROM user
GROUP BY nickname
HAVING cnt > 1;

-- 가장 최근에 만들어진 테이블 (스키마 변경 시점 추적)
SELECT TABLE_NAME, CREATE_TIME
FROM information_schema.tables
WHERE table_schema = 'android1'
ORDER BY CREATE_TIME DESC;
```

### 7.11 mysql CLI 안 들어가고 한 방에 확인하기

매번 mysql 셸에 들어갔다 나오기 귀찮으면 `-e` 옵션으로 SQL 한 줄을 박아 실행할 수 있다. **EC2 ssh 후** 한 줄:

```bash
docker compose exec mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 android1 \
  -e "SELECT COUNT(*) FROM user;"
```

여러 줄도 가능:

```bash
docker compose exec mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 android1 \
  -e "SHOW TABLES; SELECT COUNT(*) FROM user; SELECT * FROM user ORDER BY id DESC LIMIT 3;"
```

API 호출 직후 콘솔 한 줄로 행 수만 빠르게 찍어보는 용도로 편하다.

### 7.12 안전 수칙 (꼭 읽기)

| 수칙 | 이유 |
|------|------|
| `UPDATE` / `DELETE` 칠 거면 **먼저 같은 `WHERE` 로 `SELECT` 실행** | `WHERE` 빼먹으면 테이블 전체 갱신/삭제됨 |
| 다중 행 변경은 `START TRANSACTION;` → 확인 → `COMMIT;` / `ROLLBACK;` | 실수해도 `ROLLBACK` 으로 즉시 되돌릴 수 있음 |
| 운영 DB에 `DROP TABLE` / `ALTER TABLE` 직접 금지 | 스키마는 코드(JPA 엔티티)로 관리. 직접 치면 다음 앱 재기동 시 Hibernate가 어긋남을 감지해 부팅 실패 가능 |
| mysql CLI 종료는 `EXIT;` 명시 | `Ctrl+C` 는 진행 중인 쿼리만 끊고 셸은 그대로 유지 |

---

## 8. 보안 / 운영 메모

| 항목 | 현재 상태 | 메모 |
|------|----------|------|
| Spring Security | `permitAll()` (모든 요청 인증 없이 허용) | `SecurityConfig.java`. 솝커톤 1회용 / 인증 미구현 단계 기준 |
| CORS | `allowedOriginPatterns("*")` + `allowCredentials(true)` | 단기 운영용. 운영 프론트 도메인이 고정되면 화이트리스트로 좁히는 것이 안전 |
| JPA `ddl-auto` | `update` | 엔티티 변경 시 자동 ALTER. 운영 안정화 후엔 `validate` 권장 |
| `show-sql` / `format_sql` | `true` | 운영 로그 부피 큼. 안정화 후 `false`로 |
| Actuator | `/actuator/health` 만 노출, `show-details: never` | 외부에 내부 상태 안 흘림 |
| `.env` | git 미포함, 600 권한 | 노출 시 비밀번호 즉시 회전 (§6.3) |
| MySQL 외부 노출 | 없음 (`expose` 만, 호스트 매핑 없음) | 외부에서 3306 직접 접근 불가 |

---

## 9. 롤백

```bash
# EC2 ssh 후
cd ~/android1

# 직전 안정 커밋 확인
git log --oneline -10

# 해당 커밋으로 체크아웃
git checkout <이전 커밋 해시>

# 컨테이너 재빌드
docker compose up -d --build
```

DB는 §6.3 백업 파일로 복원. 운영 중 큰 변경 직전엔 백업 한 번 받아두는 게 안전하다.

---

## 10. 배포 직전 체크리스트

**DNS / 네트워크**
- [ ] `dig @8.8.8.8 sopkathon.o-r.kr +short` → EC2 퍼블릭 IP 출력
- [ ] EC2 보안그룹: 22 / 80 / 443 인바운드 개방, 그 외 모두 닫힘
- [ ] EC2에서 `curl -I http://localhost` 가 Caddy 응답 반환

**EC2**
- [ ] `docker --version` + `docker compose version` 모두 정상
- [ ] `ubuntu` 계정이 `docker` 그룹 소속 (`id ubuntu` 에 `docker` 포함)
- [ ] `EC2_APP_DIR` 에 저장소 클론 완료, `.env` 존재 + 권한 `600`

**GitHub Secrets**
- [ ] `EC2_HOST`, `EC2_USERNAME`, `EC2_SSH_KEY`, `EC2_APP_DIR` 4개 모두 등록
- [ ] Actions 탭에서 `Deploy to EC2` 워크플로 1회 수동 실행 성공

**End-to-End**
- [ ] `docker compose ps` 세 서비스 모두 `running` (mysql 은 `healthy`)
- [ ] `curl -sI https://sopkathon.o-r.kr/actuator/health` → `HTTP/2 200`
- [ ] 브라우저에서 https 접속 시 자물쇠 정상, 인증서 issuer 가 `Let's Encrypt`

---

## 11. 트러블슈팅

### 11-1. 메모리 부족으로 배포·SSH 동시 장애

**상황**

PR #34 머지 직후 `Deploy to EC2` 워크플로가 10분 만에 타임아웃으로 실패. 같은 시각에 SSH 접속도 "TCP 연결 후 banner 응답 없음" 상태로 멈췄고, 사이트(`sopkathon.o-r.kr`) 도 HTTP/HTTPS 모두 응답 없음.

**원인 분석**

`t3.small` (RAM 2 GB) 한 대에 MySQL·앱·Caddy 컨테이너가 떠 있는 상태에서, 새 PR 배포가 EC2 위에서 Gradle `compileJava` 풀빌드를 돌리며 JDK 21 컴파일러가 1 GB 넘게 추가로 점유 → **합산 ~2.5–3 GB > 2 GB**. 스왑이 없어 OOM/스래싱 상태에 진입.

이 영향으로 SSH 데몬도 banner 응답을 못 보낼 정도로 시스템이 정지 — TCP 핸드셰이크(커널)는 되지만 응용 레벨 응답 불가. 사이트 응답이 끊긴 것도 같은 원인.

지금까지의 작은 PR 은 Gradle 캐시 덕에 통과했지만, 이번 PR 은 신규 클래스 5개 추가로 캐시가 깨져 풀빌드가 돌아간 게 트리거.

**즉시 대응**

1. AWS 콘솔에서 EC2 재부팅
2. EC2 Instance Connect 로 접속해 `docker compose down && docker compose up -d --build` 수동 기동
3. 컨테이너 3종 정상 기동 확인 후 서비스 복구

**다음 번 대응 (예방)**

- 가장 손쉬운 처방: **EC2 에 스왑 추가** (2–4 GB)

  ```bash
  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  ```

  → 빌드는 느려지지만 OOM/스래싱은 피함.

- 정석은 **CI 에서 jar 빌드 → EC2 엔 jar 만 배포** — EC2 에서 JDK 컴파일 자체를 없앤다.
- 또는 **인스턴스 업그레이드** (`t3.small` → `t3.medium` 4 GB).

**교훈**

- "SSH 가 안 된다" = 네트워크 문제로 단정하지 말 것. 시스템 전체 스래싱이면 SSH 도 같이 죽는다. TCP 핸드셰이크는 되는데 banner 가 안 오면 메모리 의심을 우선순위에 둘 것.
- 작은 인스턴스에서 빌드+런타임을 같이 굴리지 말 것. CI 가 빌드 책임을 가져가는 게 정석.