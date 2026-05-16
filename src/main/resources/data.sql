-- 더미 사용자 (디자인 001 의 작성자 라인업: 박순자·김영호·이정희·최복녀)
-- 프로필 이미지는 profile_01 / profile_02 두 종을 번갈아 매핑
INSERT IGNORE INTO user (id, name, age, profile_image_url, created_at, updated_at) VALUES
    (1, '박순자', 68, '/dummy/profile_01.png', NOW(), NOW()),
    (2, '김영호', 62, '/dummy/profile_02.png', NOW(), NOW()),
    (3, '이정희', 65, '/dummy/profile_01.png', NOW(), NOW()),
    (4, '최복녀', 70, '/dummy/profile_02.png', NOW(), NOW());

-- 기존 행 백필 (INSERT IGNORE 는 기존 행을 갱신하지 않으므로 NULL 인 경우 보충)
UPDATE user SET profile_image_url = '/dummy/profile_01.png' WHERE id = 1 AND profile_image_url IS NULL;
UPDATE user SET profile_image_url = '/dummy/profile_02.png' WHERE id = 2 AND profile_image_url IS NULL;
UPDATE user SET profile_image_url = '/dummy/profile_01.png' WHERE id = 3 AND profile_image_url IS NULL;
UPDATE user SET profile_image_url = '/dummy/profile_02.png' WHERE id = 4 AND profile_image_url IS NULL;

-- 더미 노하우 카드 (사진 매핑: p1 상추모종 / p2 토마토 / p3 오이 / p4 당근 / p5 산책로(쑥) / p6 자색 무 절임)
INSERT IGNORE INTO record (id, user_id, title, photo_url, voice_duration_seconds, is_shared, location, recorded_at, created_at, updated_at) VALUES
    (1, 1, '상추 모종 심기',          '/dummy/p1.png', 30, true, '서울시 마포구 망원동',   '2026-05-14 16:19:02', NOW(), NOW()),
    (2, 2, '방울토마토 곁순 따기',     '/dummy/p2.png', 45, true, '경기도 양평군 양서면',   '2026-05-13 09:42:11', NOW(), NOW()),
    (3, 3, '오이 망 치는 법',          '/dummy/p3.png', 22, true, '전라남도 담양군',        '2026-05-12 14:05:33', NOW(), NOW()),
    (4, 4, '당근 캐기 좋은 때',        '/dummy/p4.png', 18, true, '제주특별자치도 서귀포시', '2026-05-10 10:21:48', NOW(), NOW()),
    (5, 1, '새벽 산책길에 만난 쑥',     '/dummy/p5.png', 22, true, '서울시 마포구 한강공원', '2026-05-08 05:48:20', NOW(), NOW()),
    (6, 2, '자색 무 절임 만드는 법',    '/dummy/p6.png', 38, true, '강원도 평창군',          '2026-05-05 18:02:55', NOW(), NOW());