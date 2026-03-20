# Component Dependency - 테이블오더 서비스

## 전체 시스템 의존성 구조

```
[Frontend - Customer App]          [Frontend - Admin App]
        |                                   |
        | REST API + SSE                    | REST API + SSE
        |                                   |
        +---------------+-------------------+
                        |
              [Spring Boot Backend]
              +----------+----------+
              |                     |
        [Controllers]          [Security]
              |                JwtFilter
        [Services]
    +----+----+----+----+
    |    |    |    |    |
  Auth Menu Order Table Sse
    |    |    |    |    |
        [Repositories]
              |
        [PostgreSQL]
```

---

## Backend 컴포넌트 의존성 매트릭스

| 컴포넌트 | 의존하는 컴포넌트 |
|---------|----------------|
| AuthController | AuthService |
| MenuController | MenuService |
| OrderController | OrderService |
| TableController | TableService |
| SseController | SseService |
| AuthService | StoreRepository, TableRepository, TableSessionRepository |
| MenuService | MenuRepository, CategoryRepository |
| OrderService | OrderRepository, OrderItemRepository, MenuRepository, TableSessionRepository, **SseService** |
| TableService | TableRepository, TableSessionRepository, OrderRepository, **SseService** |
| SseService | (없음 - 다른 서비스에서 호출됨) |
| JwtFilter | AuthService |

---

## Frontend - Customer App 의존성

```
App
├── AutoLoginGuard
│   └── customerApiClient (axios)
├── SetupPage
│   └── customerApiClient
├── MenuPage
│   ├── MenuCard
│   ├── useCart (hook)
│   └── customerApiClient
├── CartDrawer
│   └── useCart (hook)
├── OrderConfirmPage
│   ├── useCart (hook)
│   └── customerApiClient
└── OrderHistoryPage
    ├── useOrderSse (hook)
    │   └── SSE connection
    └── customerApiClient
```

---

## Frontend - Admin App 의존성

```
App
├── AuthGuard
│   └── useAuth (hook)
├── LoginPage
│   └── useAuth (hook)
├── DashboardPage
│   ├── TableCard
│   │   └── TableDetailModal
│   │       ├── adminApiClient
│   │       └── 주문 상태 변경/삭제
│   └── useAdminSse (hook)
│       └── SSE connection
├── TableManagePage
│   └── adminApiClient
└── MenuManagePage
    └── adminApiClient (multipart/form-data)
```

---

## 데이터 흐름 다이어그램

### 고객 주문 플로우
```
Customer Browser
    |
    | 1. 자동 로그인 (localStorage → POST /api/auth/table/login)
    v
AutoLoginGuard → 테이블 세션 토큰 획득
    |
    | 2. 메뉴 조회 (GET /api/menus)
    v
MenuPage → MenuCard 렌더링
    |
    | 3. 장바구니 담기 (localStorage)
    v
useCart → CartDrawer
    |
    | 4. 주문 확정 (POST /api/orders)
    v
OrderConfirmPage → OrderService → DB 저장
                              → SseService.sendToAdmin() → 관리자 화면 업데이트
    |
    | 5. 주문 상태 실시간 수신 (GET /api/sse/orders)
    v
useOrderSse → OrderHistoryPage 업데이트
```

### 관리자 주문 처리 플로우
```
Admin Browser
    |
    | 1. 로그인 (POST /api/auth/admin/login)
    v
LoginPage → JWT 토큰 획득 → localStorage 저장
    |
    | 2. SSE 연결 (GET /api/admin/sse/orders)
    v
useAdminSse → DashboardPage 실시간 업데이트
    |
    | 3. 주문 상태 변경 (PUT /api/admin/orders/{id}/status)
    v
TableDetailModal → OrderService → DB 업데이트
                              → SseService.sendToAdmin() → 관리자 화면
                              → SseService.sendToTable() → 고객 화면
```

---

## 모노레포 패키지 구조

```
table-order/                        # 모노레포 루트
├── backend/                        # Spring Boot
│   └── src/main/java/com/tableorder/
│       ├── auth/                   # AuthController, AuthService
│       ├── menu/                   # MenuController, MenuService
│       ├── order/                  # OrderController, OrderService
│       ├── table/                  # TableController, TableService
│       ├── sse/                    # SseController, SseService
│       ├── security/               # JwtFilter, SecurityConfig
│       ├── entity/                 # JPA Entities
│       └── repository/             # Spring Data JPA Repositories
├── frontend/
│   ├── customer/                   # React Customer App
│   │   └── src/
│   │       ├── pages/              # MenuPage, CartPage, OrderPage 등
│   │       ├── components/         # MenuCard, CartDrawer 등
│   │       ├── hooks/              # useCart, useOrderSse
│   │       └── api/                # customerApiClient
│   └── admin/                      # React Admin App
│       └── src/
│           ├── pages/              # DashboardPage, MenuManagePage 등
│           ├── components/         # TableCard, TableDetailModal 등
│           ├── hooks/              # useAdminSse, useAuth
│           └── api/                # adminApiClient
├── docker-compose.yml
└── .env.example
```

---

## 외부 의존성

| 컴포넌트 | 외부 의존성 | 용도 |
|---------|-----------|------|
| Backend | Spring Boot 3.x | 웹 프레임워크 |
| Backend | Spring Security | 인증/인가 |
| Backend | Spring Data JPA | ORM |
| Backend | PostgreSQL Driver | DB 연결 |
| Backend | jjwt | JWT 처리 |
| Backend | bcrypt (Spring Security) | 비밀번호 해싱 |
| Frontend | React 18 | UI 프레임워크 |
| Frontend | React Router v6 | 클라이언트 라우팅 |
| Frontend | Axios | HTTP 클라이언트 |
| Frontend | TypeScript | 타입 안전성 |
| Infra | Docker + Docker Compose | 컨테이너 오케스트레이션 |
| Infra | PostgreSQL 15 | 데이터베이스 |
| Infra | Nginx (선택) | 프론트엔드 정적 파일 서빙 |
