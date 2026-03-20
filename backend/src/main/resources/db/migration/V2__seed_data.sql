-- V2: 초기 시드 데이터 (개발/테스트용)
-- 관리자 비밀번호: '1234' (bcrypt)
-- 테이블 비밀번호: '1234' (bcrypt)
-- 두 해시 모두 BCryptPasswordEncoder로 생성

INSERT INTO stores (store_code, name) VALUES
    ('STORE001', '테스트 매장');

-- 관리자: admin / admin1234
INSERT INTO store_admins (store_id, username, password_hash) VALUES
    (1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- 테이블 5개 (비밀번호: '1234')
INSERT INTO tables (store_id, table_number, password_hash) VALUES
    (1, 1, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (1, 2, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (1, 3, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (1, 4, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (1, 5, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- 테이블 세션 (ACTIVE) - 테이블 로그인에 필요
INSERT INTO table_sessions (table_id, status, started_at, expires_at) VALUES
    (1, 'ACTIVE', NOW(), NOW() + INTERVAL '24 hours'),
    (2, 'ACTIVE', NOW(), NOW() + INTERVAL '24 hours'),
    (3, 'ACTIVE', NOW(), NOW() + INTERVAL '24 hours'),
    (4, 'ACTIVE', NOW(), NOW() + INTERVAL '24 hours'),
    (5, 'ACTIVE', NOW(), NOW() + INTERVAL '24 hours');

-- 카테고리
INSERT INTO categories (store_id, name, display_order) VALUES
    (1, '메인 메뉴', 1),
    (1, '사이드', 2),
    (1, '음료', 3);

-- 메뉴
INSERT INTO menus (store_id, category_id, name, price, description, display_order) VALUES
    (1, 1, '불고기 버거', 8900, '국내산 소고기 패티', 1),
    (1, 1, '치킨 버거', 7900, '바삭한 치킨 패티', 2),
    (1, 2, '감자튀김', 3000, '바삭한 감자튀김', 1),
    (1, 2, '양파링', 3500, '바삭한 양파링', 2),
    (1, 3, '콜라', 2000, '시원한 콜라', 1),
    (1, 3, '아메리카노', 3000, '진한 아메리카노', 2);
