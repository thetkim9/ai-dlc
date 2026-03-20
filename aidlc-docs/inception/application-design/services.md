# Services - 테이블오더 서비스

## 서비스 레이어 개요

Spring Boot 백엔드는 Controller → Service → Repository 3계층 구조를 따릅니다.
각 Service는 단일 비즈니스 도메인을 담당하며, 도메인 간 오케스트레이션이 필요한 경우 Service 간 호출로 처리합니다.

---

## 서비스 정의

### 1. AuthService
**도메인**: 인증 및 세션 관리
**책임**:
- 관리자 로그인 인증 (bcrypt 비밀번호 검증)
- JWT 토큰 생성/검증/파싱
- 테이블 세션 토큰 생성/검증
- 로그인 시도 횟수 제한 (Redis 또는 in-memory)

**의존 서비스**: 없음 (독립적)
**의존 Repository**: StoreRepository, TableRepository, TableSessionRepository

---

### 2. MenuService
**도메인**: 메뉴 및 카테고리 관리
**책임**:
- 메뉴 CRUD 처리
- 이미지 파일 저장/삭제 (로컬 파일시스템)
- 메뉴 노출 순서 관리
- 입력값 유효성 검증 (가격 범위, 필수 필드)

**의존 서비스**: 없음 (독립적)
**의존 Repository**: MenuRepository, CategoryRepository

---

### 3. OrderService
**도메인**: 주문 처리
**책임**:
- 주문 생성 및 검증 (세션 유효성, 메뉴 존재 여부)
- 주문 상태 전이 관리
- 주문 삭제 및 총액 재계산
- 주문 생성/상태 변경 시 SSE 이벤트 발행

**의존 서비스**: SseService (이벤트 발행)
**의존 Repository**: OrderRepository, OrderItemRepository, MenuRepository, TableSessionRepository

---

### 4. TableService
**도메인**: 테이블 및 세션 라이프사이클
**책임**:
- 테이블 초기 설정 (비밀번호 설정, 세션 생성)
- 테이블 이용 완료 처리 (세션 종료, 주문 이력 이동)
- 과거 주문 이력 조회

**의존 서비스**: OrderService (주문 이력 이동 시)
**의존 Repository**: TableRepository, TableSessionRepository, OrderRepository

---

### 5. SseService
**도메인**: 실시간 이벤트 스트리밍
**책임**:
- SSE Emitter 풀 관리 (관리자/고객 분리)
- 이벤트 브로드캐스트
- 연결 만료/오류 처리

**의존 서비스**: 없음 (독립적, 다른 서비스에서 호출됨)
**의존 Repository**: 없음 (in-memory Emitter 관리)

---

## 서비스 오케스트레이션 패턴

### 주문 생성 플로우
```
OrderController
    └── OrderService.createOrder()
            ├── TableSessionRepository.findById() - 세션 유효성 확인
            ├── MenuRepository.findAllById() - 메뉴 존재 확인
            ├── OrderRepository.save() - 주문 저장
            ├── OrderItemRepository.saveAll() - 주문 항목 저장
            └── SseService.sendToAdmin() - 관리자에게 신규 주문 이벤트
```

### 주문 상태 변경 플로우
```
OrderController
    └── OrderService.updateOrderStatus()
            ├── OrderRepository.findById() - 주문 조회
            ├── OrderRepository.save() - 상태 업데이트
            ├── SseService.sendToAdmin() - 관리자 화면 업데이트
            └── SseService.sendToTable() - 고객 화면 상태 업데이트
```

### 테이블 이용 완료 플로우
```
TableController
    └── TableService.completeSession()
            ├── TableSessionRepository.findActiveSession() - 현재 세션 조회
            ├── OrderRepository.findBySessionId() - 현재 세션 주문 조회
            ├── OrderRepository.markAsHistory() - 주문 이력으로 이동
            ├── TableSessionRepository.closeSession() - 세션 종료
            └── SseService.sendToAdmin() - 관리자 대시보드 업데이트
```

---

## SSE 이벤트 타입 정의

| 이벤트명 | 발행 주체 | 수신 대상 | 데이터 |
|---------|---------|---------|--------|
| `new-order` | OrderService | 관리자 SSE | OrderResponse |
| `order-status-changed` | OrderService | 관리자 SSE + 고객 SSE | {orderId, status} |
| `order-deleted` | OrderService | 관리자 SSE | {orderId, tableId} |
| `table-completed` | TableService | 관리자 SSE | {tableId} |

---

## 보안 서비스 설계

### Spring Security 설정
```
Public endpoints (인증 불필요):
  POST /api/auth/admin/login
  POST /api/auth/table/login
  GET  /api/categories
  GET  /api/menus

Table session endpoints (테이블 토큰 필요):
  POST /api/orders
  GET  /api/orders/session
  GET  /api/sse/orders

Admin endpoints (JWT 필요):
  GET/POST/PUT/DELETE /api/admin/**
  GET /api/admin/sse/orders
```

### 토큰 전달 방식
- 관리자 JWT: `Authorization: Bearer {token}` 헤더
- 테이블 세션 토큰: `Authorization: Bearer {token}` 헤더 (별도 claim으로 구분)
