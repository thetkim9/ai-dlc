# Business Rules - Backend (Unit 1)

## 인증 관련 규칙

### BR-AUTH-01: 관리자 로그인
- storeCode + username + password 조합으로 인증
- 비밀번호는 bcrypt로 검증
- 성공 시 JWT 발급 (만료: 16시간)
- JWT payload: `{ adminId, storeId, role: "ADMIN" }`

### BR-AUTH-02: 로그인 시도 제한
- 동일 storeCode + username 조합으로 5회 연속 실패 시 15분 잠금
- 잠금 상태에서 시도 시 429 Too Many Requests 반환
- 성공 시 시도 횟수 초기화
- 구현: in-memory Map (소규모 기준, Redis 불필요)

### BR-AUTH-03: 테이블 자동 로그인
- storeCode + tableNumber + password 조합으로 인증
- 해당 테이블의 ACTIVE 세션이 있어야 로그인 가능
- 세션이 없으면 401 반환 (관리자에게 초기 설정 요청 안내)
- 성공 시 테이블 세션 토큰 발급
- 토큰 payload: `{ tableId, sessionId, role: "TABLE" }`

### BR-AUTH-04: JWT 검증
- 모든 인증 필요 API에서 Authorization Bearer 토큰 검증
- 만료된 토큰: 401 Unauthorized
- role 기반 접근 제어: ADMIN은 /api/admin/**, TABLE은 /api/orders, /api/sse/orders

---

## 테이블 세션 관련 규칙

### BR-SESSION-01: 세션 생성
- 관리자가 테이블 초기 설정 시 ACTIVE 세션 생성
- 동일 테이블에 ACTIVE 세션이 이미 있으면 기존 세션 종료 후 새 세션 생성
- 세션 만료 시각 = 생성 시각 + 16시간

### BR-SESSION-02: 세션 종료 (이용 완료)
- 관리자가 이용 완료 처리 시:
  1. 현재 세션의 모든 Order.isHistory = true 로 변경
  2. TableSession.status = COMPLETED, completedAt = 현재 시각
  3. SSE로 관리자에게 테이블 리셋 이벤트 발행
- 이용 완료 후 고객이 주문 내역 조회 시 빈 목록 반환

### BR-SESSION-03: 세션 만료 처리
- 세션 토큰 검증 시 TableSession.expiresAt 확인
- 만료된 세션 토큰: 401 반환
- 만료된 세션으로 주문 시도: 403 반환

---

## 주문 관련 규칙

### BR-ORDER-01: 주문 생성
- 유효한 테이블 세션 토큰 필요
- 주문 항목이 1개 이상이어야 함
- 각 menuId가 해당 매장의 유효한 메뉴인지 검증
- Menu.available = false인 메뉴 주문 시 400 반환
- OrderItem에 주문 시점의 menuName, unitPrice 스냅샷 저장 (메뉴 수정 후에도 주문 내역 보존)
- totalAmount = sum(quantity * unitPrice)
- 초기 상태: PENDING

### BR-ORDER-02: 주문 상태 전이
- PENDING → PREPARING → COMPLETED (단방향)
- 역방향 전이 불가 (400 Bad Request)
- 상태 변경 시 SSE 이벤트 발행:
  - 관리자 채널: order-status-changed
  - 고객 채널 (해당 세션): order-status-changed

### BR-ORDER-03: 주문 삭제 (관리자 직권)
- 관리자만 가능
- 삭제 후 해당 테이블의 현재 세션 총 주문액 재계산
- SSE로 관리자에게 order-deleted 이벤트 발행
- 물리 삭제 (soft delete 불필요)

### BR-ORDER-04: 주문 내역 조회 (고객)
- 현재 테이블 세션 ID 기준으로 조회
- isHistory = false인 주문만 반환
- 주문 시각 오름차순 정렬

### BR-ORDER-05: 과거 주문 내역 조회 (관리자)
- isHistory = true인 주문 조회
- 날짜 필터: orderedAt 기준
- 시간 역순 정렬

---

## 메뉴 관련 규칙

### BR-MENU-01: 메뉴 등록
- 필수 필드: name, price, categoryId
- price > 0 검증
- 이미지 업로드 시: jpg/png/webp만 허용, 최대 5MB
- displayOrder: 해당 카테고리 내 마지막 순서로 자동 설정

### BR-MENU-02: 메뉴 삭제
- 현재 ACTIVE 세션의 주문에 포함된 메뉴 삭제 시 경고 (강제 삭제 가능)
- 이미지 파일도 함께 삭제

### BR-MENU-03: 메뉴 순서 조정
- 동일 카테고리 내 메뉴들의 displayOrder 일괄 업데이트
- 요청 body: [{ menuId, displayOrder }] 배열

---

## 이미지 파일 관련 규칙

### BR-FILE-01: 파일 저장
- 저장 경로: `{UPLOAD_DIR}/{storeId}/{UUID}.{ext}`
- 원본 파일명 사용 금지 (보안)
- 응답에 접근 가능한 URL 반환: `/uploads/{storeId}/{filename}`

### BR-FILE-02: 파일 접근
- `/uploads/**` 경로는 인증 없이 접근 가능 (공개 정적 파일)
- Spring Boot WebMvcConfigurer로 정적 파일 서빙 설정

---

## SSE 관련 규칙

### BR-SSE-01: 연결 관리
- 관리자 SSE: storeId 기준으로 Emitter 관리
- 고객 SSE: sessionId 기준으로 Emitter 관리
- Emitter 타임아웃: 30분
- 연결 종료 시 Emitter 풀에서 제거

### BR-SSE-02: 이벤트 발행
- 이벤트 발행 실패 시 해당 Emitter 제거 (클라이언트 재연결 유도)
- 이벤트 데이터: JSON 직렬화
