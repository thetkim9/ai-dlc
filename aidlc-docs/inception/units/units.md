# Units of Work - 테이블오더 서비스

## 분해 전략

모노레포 구조 내 3개 독립 Unit으로 분해합니다.
각 Unit은 독립적으로 설계(Functional Design → NFR → Infrastructure → Code Generation)되며,
최종 Docker Compose로 통합됩니다.

---

## Unit 1: Backend

**디렉토리**: `backend/`
**기술 스택**: Java 21 + Spring Boot 3.4.x + PostgreSQL 15
**배포**: Docker 컨테이너 (포트 8080)
**상태**: ✅ COMPLETED

### 책임
- REST API 전체 제공 (인증, 메뉴, 주문, 테이블)
- SSE 실시간 이벤트 스트리밍
- JWT 및 테이블 세션 토큰 인증/인가
- 비즈니스 로직 처리 (주문 생성, 세션 관리, 이용 완료)
- 이미지 파일 업로드 및 정적 파일 서빙
- PostgreSQL 데이터 영속성 관리

### 관련 Stories
C1-1, C2-1, C4-1, C5-1, C5-2, A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3

### 완료 기준
- [x] 모든 REST API 엔드포인트 구현 및 동작
- [x] SSE 연결 및 이벤트 발행 동작
- [x] JWT/세션 인증 동작
- [x] Docker 컨테이너로 실행 가능
- [x] PostgreSQL 연결 및 스키마 생성
- [x] 단위 테스트 전체 통과

---

## Unit 2: Frontend - Customer App

**디렉토리**: `frontend/customer/`
**기술 스택**: React 18 + TypeScript + Vite
**배포**: Docker 컨테이너 (Nginx, 포트 3000)
**상태**: 🔲 PENDING

### 책임
- 고객용 주문 UI 전체 제공
- 테이블 자동 로그인 (localStorage 기반)
- 메뉴 탐색 및 장바구니 관리
- 주문 생성 및 확정
- 주문 내역 조회 및 SSE 실시간 상태 업데이트

### 관련 Stories
C1-1, C2-1, C2-2, C3-1, C3-2, C4-1, C5-1, C5-2

**Must Have**: C1-1, C2-1, C3-1, C4-1, C5-1, C5-2
**Should Have**: C2-2, C3-2

### 주요 컴포넌트
- Pages: MenuPage, OrderConfirmPage, OrderHistoryPage, SetupPage
- Components: MenuCard, CartDrawer, CategoryTabs
- Hooks: useCart, useOrderSse
- API: customerApiClient (axios + 테이블 토큰 interceptor)

### 완료 기준
- [ ] 자동 로그인 → 메뉴 화면 정상 동작
- [ ] 장바구니 추가/수정/삭제 및 localStorage 유지
- [ ] 주문 생성 → 주문 내역 화면 이동
- [ ] SSE 연결로 주문 상태 실시간 업데이트
- [ ] Docker 컨테이너로 실행 가능

---

## Unit 3: Frontend - Admin App

**디렉토리**: `frontend/admin/`
**기술 스택**: React 18 + TypeScript + Vite
**배포**: Docker 컨테이너 (Nginx, 포트 3001)
**상태**: 🔲 PENDING

### 책임
- 관리자용 매장 운영 UI 전체 제공
- JWT 기반 관리자 인증 (16시간 세션)
- 실시간 주문 모니터링 대시보드 (SSE)
- 테이블 관리 (초기 설정, 주문 삭제, 이용 완료, 과거 내역)
- 메뉴 관리 CRUD (이미지 파일 업로드 포함)

### 관련 Stories
A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3

**Must Have**: A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A4-1, A4-2
**Should Have**: A3-4
**Could Have**: A4-3

### 주요 컴포넌트
- Pages: LoginPage, DashboardPage, TableManagePage, MenuManagePage
- Components: TableCard, TableDetailModal, MenuForm
- Hooks: useAdminSse, useAuth
- API: adminApiClient (axios + JWT interceptor)

### 완료 기준
- [ ] 관리자 로그인 → JWT 저장 → 대시보드 접근
- [ ] SSE 연결로 신규 주문 실시간 수신 (2초 이내)
- [ ] 주문 상태 변경 동작
- [ ] 테이블 이용 완료 처리 동작
- [ ] 메뉴 CRUD + 이미지 업로드 동작
- [ ] Docker 컨테이너로 실행 가능

---

## 통합 인프라

**파일**: `docker-compose.yml`, `.env.example`

```
docker-compose.yml
├── postgres      (PostgreSQL 15, 내부 포트 5432)
├── backend       (Spring Boot, 포트 8080)
├── customer-app  (Nginx + React, 포트 3000)
└── admin-app     (Nginx + React, 포트 3001)
```

---

## 실행 순서

| 순서 | Unit | 의존성 |
|------|------|--------|
| 1 | Unit 1 Backend | 없음 (선행 필수) |
| 2 | Unit 2 Frontend-Customer | Unit 1 API 완료 후 |
| 3 | Unit 3 Frontend-Admin | Unit 1 API 완료 후 |
