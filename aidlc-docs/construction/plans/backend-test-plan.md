# Test Plan - Backend (Unit 1)

## Unit Overview
- **Unit**: backend
- **Test Framework**: JUnit 5 + Mockito + Spring Boot Test
- **Stories**: C1-1, C2-1, C4-1, C5-1, C5-2, A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3

---

## AuthService Tests

### AuthService.authenticateAdmin()
- **TC-BE-001**: 유효한 자격증명으로 관리자 로그인 성공
  - Given: 유효한 storeCode, username, bcrypt 해시된 비밀번호가 DB에 존재
  - When: authenticateAdmin(storeCode, username, rawPassword) 호출
  - Then: JWT 토큰이 포함된 AdminLoginResponse 반환
  - Story: A1-1 | Status: ⬜

- **TC-BE-002**: 잘못된 비밀번호로 로그인 실패
  - Given: 유효한 storeCode, username이 존재하지만 비밀번호 불일치
  - When: authenticateAdmin() 호출
  - Then: UnauthorizedException 발생
  - Story: A1-1 | Status: ⬜

- **TC-BE-003**: 존재하지 않는 매장으로 로그인 실패
  - Given: 존재하지 않는 storeCode
  - When: authenticateAdmin() 호출
  - Then: UnauthorizedException 발생
  - Story: A1-1 | Status: ⬜

- **TC-BE-004**: 5회 실패 후 로그인 잠금
  - Given: 동일 계정으로 5회 연속 실패
  - When: 6번째 시도
  - Then: TooManyRequestsException 발생
  - Story: A1-1 | Status: ⬜

### AuthService.authenticateTable()
- **TC-BE-005**: 유효한 테이블 자격증명으로 로그인 성공
  - Given: 유효한 storeCode, tableNumber, password + ACTIVE 세션 존재
  - When: authenticateTable() 호출
  - Then: 테이블 세션 토큰 포함 TableLoginResponse 반환
  - Story: C1-1 | Status: ⬜

- **TC-BE-006**: ACTIVE 세션 없을 때 테이블 로그인 실패
  - Given: 유효한 자격증명이지만 ACTIVE 세션 없음
  - When: authenticateTable() 호출
  - Then: UnauthorizedException 발생
  - Story: C1-1 | Status: ⬜

---

## MenuService Tests

### MenuService.createMenu()
- **TC-BE-007**: 유효한 데이터로 메뉴 등록 성공
  - Given: 유효한 storeId, 메뉴명, 가격 > 0, 유효한 categoryId
  - When: createMenu() 호출
  - Then: 저장된 MenuResponse 반환, displayOrder 자동 설정
  - Story: A4-1 | Status: ⬜

- **TC-BE-008**: 가격이 0 이하일 때 메뉴 등록 실패
  - Given: price = 0
  - When: createMenu() 호출
  - Then: ValidationException 발생
  - Story: A4-1 | Status: ⬜

- **TC-BE-009**: 메뉴명 누락 시 등록 실패
  - Given: name = null 또는 빈 문자열
  - When: createMenu() 호출
  - Then: ValidationException 발생
  - Story: A4-1 | Status: ⬜

### MenuService.deleteMenu()
- **TC-BE-010**: 존재하는 메뉴 삭제 성공
  - Given: 유효한 storeId, menuId
  - When: deleteMenu() 호출
  - Then: 메뉴 삭제됨, 이미지 파일 삭제됨
  - Story: A4-2 | Status: ⬜

- **TC-BE-011**: 존재하지 않는 메뉴 삭제 시 실패
  - Given: 존재하지 않는 menuId
  - When: deleteMenu() 호출
  - Then: NotFoundException 발생
  - Story: A4-2 | Status: ⬜

---

## OrderService Tests

### OrderService.createOrder()
- **TC-BE-012**: 유효한 주문 생성 성공
  - Given: 유효한 세션, 존재하는 메뉴 목록, available=true
  - When: createOrder() 호출
  - Then: OrderResponse 반환, totalAmount 정확히 계산, SSE 이벤트 발행
  - Story: C4-1 | Status: ⬜

- **TC-BE-013**: 빈 주문 항목으로 주문 실패
  - Given: 빈 items 목록
  - When: createOrder() 호출
  - Then: ValidationException 발생
  - Story: C4-1 | Status: ⬜

- **TC-BE-014**: 존재하지 않는 메뉴로 주문 실패
  - Given: 존재하지 않는 menuId 포함
  - When: createOrder() 호출
  - Then: NotFoundException 발생
  - Story: C4-1 | Status: ⬜

- **TC-BE-015**: 만료된 세션으로 주문 실패
  - Given: 만료된 TableSession
  - When: createOrder() 호출
  - Then: ForbiddenException 발생
  - Story: C4-1 | Status: ⬜

### OrderService.updateOrderStatus()
- **TC-BE-016**: PENDING → PREPARING 상태 변경 성공
  - Given: PENDING 상태 주문
  - When: updateOrderStatus(orderId, PREPARING) 호출
  - Then: 상태 업데이트, SSE 이벤트 발행 (관리자 + 고객)
  - Story: A2-2 | Status: ⬜

- **TC-BE-017**: COMPLETED → PENDING 역방향 전이 실패
  - Given: COMPLETED 상태 주문
  - When: updateOrderStatus(orderId, PENDING) 호출
  - Then: BadRequestException 발생
  - Story: A2-2 | Status: ⬜

### OrderService.getOrdersBySession()
- **TC-BE-018**: 현재 세션 주문만 반환
  - Given: 세션에 isHistory=false 주문 2개, isHistory=true 주문 1개
  - When: getOrdersBySession(sessionId) 호출
  - Then: isHistory=false 주문 2개만 반환, 시간 오름차순
  - Story: C5-1 | Status: ⬜

---

## TableService Tests

### TableService.setupTable()
- **TC-BE-019**: 테이블 초기 설정 성공 (신규 세션 생성)
  - Given: 유효한 storeId, tableId, 기존 ACTIVE 세션 없음
  - When: setupTable() 호출
  - Then: 새 ACTIVE TableSession 생성, 만료 시각 = now + 16시간
  - Story: A3-1 | Status: ⬜

- **TC-BE-020**: 기존 ACTIVE 세션 있을 때 재설정 (기존 세션 종료 후 신규 생성)
  - Given: 기존 ACTIVE 세션 존재
  - When: setupTable() 호출
  - Then: 기존 세션 COMPLETED 처리, 새 ACTIVE 세션 생성
  - Story: A3-1 | Status: ⬜

### TableService.completeSession()
- **TC-BE-021**: 이용 완료 처리 성공
  - Given: ACTIVE 세션 + isHistory=false 주문 3개
  - When: completeSession() 호출
  - Then: 모든 주문 isHistory=true, 세션 COMPLETED, SSE 이벤트 발행
  - Story: A3-3 | Status: ⬜

- **TC-BE-022**: ACTIVE 세션 없을 때 이용 완료 실패
  - Given: ACTIVE 세션 없음
  - When: completeSession() 호출
  - Then: NotFoundException 발생
  - Story: A3-3 | Status: ⬜

---

## FileStorageService Tests

### FileStorageService.saveFile()
- **TC-BE-023**: 유효한 이미지 파일 저장 성공
  - Given: jpg 파일, 1MB
  - When: saveFile(storeId, file) 호출
  - Then: UUID 기반 파일명으로 저장, 접근 URL 반환
  - Story: A4-1 | Status: ⬜

- **TC-BE-024**: 허용되지 않는 확장자 파일 저장 실패
  - Given: .exe 파일
  - When: saveFile() 호출
  - Then: ValidationException 발생
  - Story: A4-1 | Status: ⬜

- **TC-BE-025**: 5MB 초과 파일 저장 실패
  - Given: 6MB jpg 파일
  - When: saveFile() 호출
  - Then: ValidationException 발생
  - Story: A4-1 | Status: ⬜

---

## API Layer Tests (Integration)

### AuthController
- **TC-BE-026**: POST /api/auth/admin/login - 성공 (200)
  - Story: A1-1 | Status: ⬜
- **TC-BE-027**: POST /api/auth/admin/login - 실패 (401)
  - Story: A1-1 | Status: ⬜
- **TC-BE-028**: POST /api/auth/table/login - 성공 (200)
  - Story: C1-1 | Status: ⬜

### OrderController
- **TC-BE-029**: POST /api/orders - 성공 (201)
  - Story: C4-1 | Status: ⬜
- **TC-BE-030**: POST /api/orders - 인증 없음 (401)
  - Story: C4-1 | Status: ⬜
- **TC-BE-031**: PUT /api/admin/orders/{id}/status - 성공 (200)
  - Story: A2-2 | Status: ⬜

### MenuController
- **TC-BE-032**: GET /api/menus - 인증 없이 접근 가능 (200)
  - Story: C2-1 | Status: ⬜
- **TC-BE-033**: POST /api/admin/menus - TABLE 토큰으로 접근 거부 (403)
  - Story: A4-1 | Status: ⬜

---

## Requirements Coverage

| Story | Test Cases | Status |
|-------|-----------|--------|
| C1-1 테이블 자동 로그인 | TC-BE-005, TC-BE-006, TC-BE-028 | ⬜ |
| C2-1 메뉴 조회 | TC-BE-032 | ⬜ |
| C4-1 주문 확정 | TC-BE-012~015, TC-BE-029, TC-BE-030 | ⬜ |
| C5-1 주문 내역 조회 | TC-BE-018 | ⬜ |
| C5-2 실시간 상태 | TC-BE-016 (SSE 발행 검증) | ⬜ |
| A1-1 관리자 로그인 | TC-BE-001~004, TC-BE-026, TC-BE-027 | ⬜ |
| A2-2 주문 상태 변경 | TC-BE-016, TC-BE-017, TC-BE-031 | ⬜ |
| A3-1 테이블 초기 설정 | TC-BE-019, TC-BE-020 | ⬜ |
| A3-3 이용 완료 처리 | TC-BE-021, TC-BE-022 | ⬜ |
| A4-1 메뉴 등록 | TC-BE-007~009, TC-BE-023~025 | ⬜ |
| A4-2 메뉴 수정/삭제 | TC-BE-010, TC-BE-011 | ⬜ |
