# Code Summary - Backend (Unit 1)

## 구현 완료 현황

### 기술 스택
- Java 21 (LTS)
- Spring Boot 3.4.x
- PostgreSQL 15
- Flyway (DB 마이그레이션)
- JWT (jjwt 0.12.x)
- SSE (Server-Sent Events)
- Docker + Docker Compose

### 패키지 구조

```
backend/src/main/java/com/tableorder/
├── TableOrderApplication.java
├── auth/
│   ├── AuthController.java         # POST /api/auth/admin/login, /api/auth/table/login
│   ├── AuthService.java            # 인증 로직, 로그인 시도 제한
│   └── dto/
│       ├── AdminLoginRequest.java
│       ├── AdminLoginResponse.java
│       ├── TableLoginRequest.java
│       └── TableLoginResponse.java
├── menu/
│   ├── MenuController.java         # CRUD /api/menus, /api/categories, /api/admin/menus
│   ├── MenuService.java
│   ├── FileStorageService.java     # 이미지 파일 저장/삭제
│   └── dto/
│       ├── CategoryResponse.java
│       ├── MenuCreateRequest.java
│       ├── MenuUpdateRequest.java
│       ├── MenuOrderRequest.java
│       └── MenuResponse.java
├── order/
│   ├── OrderController.java        # POST /api/orders, /api/admin/orders
│   ├── OrderService.java
│   └── dto/
│       ├── OrderCreateRequest.java
│       ├── OrderItemRequest.java
│       ├── OrderItemResponse.java
│       ├── OrderResponse.java
│       └── OrderStatusUpdateRequest.java
├── table/
│   ├── TableController.java        # /api/admin/tables
│   ├── TableService.java
│   └── dto/
│       ├── TableSetupRequest.java
│       ├── TableSetupResponse.java
│       ├── TableSummaryResponse.java
│       └── OrderHistoryResponse.java
├── sse/
│   ├── SseController.java          # GET /api/sse/orders, /api/admin/sse/orders
│   └── SseService.java             # Emitter 풀 관리, 이벤트 발행
├── entity/
│   ├── Store.java
│   ├── StoreAdmin.java
│   ├── TableEntity.java
│   ├── TableSession.java
│   ├── Category.java
│   ├── Menu.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java            # PENDING, PREPARING, COMPLETED
│   └── SessionStatus.java          # ACTIVE, COMPLETED
├── repository/
│   ├── StoreRepository.java
│   ├── StoreAdminRepository.java
│   ├── TableRepository.java
│   ├── TableSessionRepository.java
│   ├── CategoryRepository.java
│   ├── MenuRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── security/
│   ├── SecurityConfig.java         # Spring Security 설정, CORS
│   ├── JwtUtil.java                # JWT 생성/파싱
│   ├── JwtAuthenticationFilter.java
│   ├── AdminPrincipal.java
│   └── TablePrincipal.java
├── common/
│   └── exception/
│       ├── ApiException.java       # 비즈니스 예외
│       └── GlobalExceptionHandler.java
└── config/
    └── WebConfig.java              # 정적 파일 서빙 (/uploads/**)
```

### DB 마이그레이션

```
backend/src/main/resources/db/migration/
├── V1__init_schema.sql    # 테이블 생성 (stores, store_admins, tables, ...)
└── V2__seed_data.sql      # 초기 데이터 (매장 1개, 관리자 1명, 테이블 5개, 카테고리 3개)
```

### 테스트 구조

```
backend/src/test/java/com/tableorder/
├── auth/
│   ├── AuthServiceTest.java        # 단위 테스트 (Mockito)
│   └── AuthControllerTest.java     # 통합 테스트 (@WebMvcTest)
├── menu/
│   ├── MenuServiceTest.java
│   └── FileStorageServiceTest.java
├── order/
│   ├── OrderServiceTest.java
│   └── OrderControllerTest.java
└── table/
    └── TableServiceTest.java
```

## API 엔드포인트 목록

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| POST | /api/auth/admin/login | 없음 | 관리자 로그인 |
| POST | /api/auth/table/login | 없음 | 테이블 로그인 |
| GET | /api/categories | 없음 | 카테고리 목록 |
| GET | /api/menus | 없음 | 메뉴 목록 |
| POST | /api/admin/menus | ADMIN | 메뉴 등록 |
| PUT | /api/admin/menus/{id} | ADMIN | 메뉴 수정 |
| DELETE | /api/admin/menus/{id} | ADMIN | 메뉴 삭제 |
| PUT | /api/admin/menus/order | ADMIN | 메뉴 순서 변경 |
| POST | /api/orders | TABLE | 주문 생성 |
| GET | /api/orders/session | TABLE | 현재 세션 주문 조회 |
| GET | /api/admin/tables | ADMIN | 테이블 목록 |
| POST | /api/admin/tables/{id}/setup | ADMIN | 테이블 초기 설정 |
| POST | /api/admin/tables/{id}/complete | ADMIN | 이용 완료 |
| GET | /api/admin/tables/{id}/history | ADMIN | 주문 이력 |
| GET | /api/admin/tables/{id}/orders | ADMIN | 현재 주문 조회 |
| PUT | /api/admin/orders/{id}/status | ADMIN | 주문 상태 변경 |
| DELETE | /api/admin/orders/{id} | ADMIN | 주문 삭제 |
| GET | /api/sse/orders | TABLE | 고객 SSE 연결 |
| GET | /api/admin/sse/orders | ADMIN | 관리자 SSE 연결 |
| GET | /actuator/health | 없음 | 헬스체크 |

## 주요 구현 결정사항

- Lombok 미사용: 명시적 생성자/getter로 대체 (환경 호환성)
- 로그인 시도 제한: In-memory ConcurrentHashMap (소규모 기준)
- SSE Emitter 풀: In-memory ConcurrentHashMap (재시작 시 클라이언트 재연결)
- 이미지 저장: 로컬 파일시스템 + Docker volume
- 트랜잭션: Service 레이어에서 관리
