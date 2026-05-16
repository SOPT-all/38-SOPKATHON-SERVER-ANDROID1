-- 더미 사용자 6명 (50~60대 페르소나, 홀수 id=profile_01 / 짝수 id=profile_02)
INSERT IGNORE INTO user (id, name, age, profile_image_url, created_at, updated_at) VALUES
    (1, '박순자', 58, '/dummy/profile_01.png', NOW(), NOW()),
    (2, '김영호', 62, '/dummy/profile_02.png', NOW(), NOW()),
    (3, '이정희', 55, '/dummy/profile_01.png', NOW(), NOW()),
    (4, '정명숙', 64, '/dummy/profile_02.png', NOW(), NOW()),
    (5, '한상철', 59, '/dummy/profile_01.png', NOW(), NOW()),
    (6, '윤미경', 61, '/dummy/profile_02.png', NOW(), NOW());

-- 기존 EC2 행(id 1~4) 을 새 라인업으로 덮어쓰기 (재기동마다 idempotent)
UPDATE user SET name = '박순자', age = 58, profile_image_url = '/dummy/profile_01.png' WHERE id = 1;
UPDATE user SET name = '김영호', age = 62, profile_image_url = '/dummy/profile_02.png' WHERE id = 2;
UPDATE user SET name = '이정희', age = 55, profile_image_url = '/dummy/profile_01.png' WHERE id = 3;
UPDATE user SET name = '정명숙', age = 64, profile_image_url = '/dummy/profile_02.png' WHERE id = 4;

-- 더미 노하우 카드 (사진 매핑: p1 상추모종 / p2 토마토 / p3 오이 / p4 당근 / p5 산책로(쑥) / p6 자색 무 절임)
INSERT IGNORE INTO record (id, user_id, title, photo_url, voice_duration_seconds, is_shared, location, recorded_at, created_at, updated_at) VALUES
    (1, 1, '상추 모종 심기',          '/dummy/p1.png', 30, true, '서울시 마포구 망원동',   '2026-05-14 16:19:02', NOW(), NOW()),
    (2, 2, '방울토마토 곁순 따기',     '/dummy/p2.png', 45, true, '경기도 양평군 양서면',   '2026-05-13 09:42:11', NOW(), NOW()),
    (3, 3, '오이 망 치는 법',          '/dummy/p3.png', 22, true, '전라남도 담양군',        '2026-05-12 14:05:33', NOW(), NOW()),
    (4, 4, '당근 캐기 좋은 때',        '/dummy/p4.png', 18, true, '제주특별자치도 서귀포시', '2026-05-10 10:21:48', NOW(), NOW()),
    (5, 5, '새벽 산책길에 만난 쑥',     '/dummy/p5.png', 22, true, '서울시 마포구 한강공원', '2026-05-08 05:48:20', NOW(), NOW()),
    (6, 6, '자색 무 절임 만드는 법',    '/dummy/p6.png', 38, true, '강원도 평창군',          '2026-05-05 18:02:55', NOW(), NOW());

-- record 5·6 의 user_id 를 (1, 2) → (5, 6) 으로 재매핑 (기존 EC2 행 보정)
UPDATE record SET user_id = 5 WHERE id = 5;
UPDATE record SET user_id = 6 WHERE id = 6;