# Code Generation Plan - Backend (Unit 1)

## 대상 Unit
- **Unit**: Backend (Spring Boot)
- **코드 위치**: `backend/` (워크스페이스 루트)
- **관련 Stories**: C1-1, C2-1, C4-1, C5-1, C5-2, A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3

---

## 실행 체크리스트

### Step 1: 프로젝트 구조 설정
- [ ] 1.1 `backend/` 디렉토리 생성
- [ ] 1.2 `pom.xml` 생성 (Spring Boot 3.2.x, 모든 의존성 포함)
- [ ] 1.3 `src/main/resources/application.yml` 생성
- [ ] 1.4 `src/main/resources/application-dev.yml` 생성
- [ ] 1.5 메인 클래스 `TableOrderApplication.java` 생성
- [ ] 1.6 `Dockerfile` 생성
- [ ] 1.7 `.env.example` 생성 (루트)
- [ ] 1.8 `docker-compose.yml` 생성 (루트, 전체 스택)

### Step 2: Entity 및 Repository 생성
- [ ] 2.1 `Store.java` Entity
- [ ] 2.2 `StoreAdmin.java` Entity
- [ ] 2.3 `TableEntity.java` Entity
- [ ] 2.4 `TableSession.java` Entity + `SessionStatus` enum
- [ ] 2.5 `Category.java` Entity
- [ ] 2.6 `Menu.java` Entity
- [ ] 2.7 `Order.java` Entity + `OrderStatus` enum
- [ ] 2.8 `OrderItem.java` Entity
- [ ] 2.9 모든 Repository 인터페이스 (Spring Data JPA)
- [ ] 2.10 Flyway 마이그레이션 스크립트 `V1__init_schema.sql`
- [ ] 2.11 초기 데이터 스크립트 `V2__seed_data.sql` (샘플 매장/메뉴)

### Step 3: 공통 컴포넌트 생성
- [ ] 3.1 `BusinessException.java` + 에러 코드 enum
- [ ] 3.2 `GlobalExceptionHandler.java` (`@RestControllerAdvice`)
- [ ] 3.3 `ErrorResponse.java` record
- [ ] 3.4 `JwtUtil.java` (토큰 생성/파싱/검증)
- [ ] 3.5 `JwtAuthenticationFilter.java` (`OncePerRequestFilter`)
- [ ] 3.6 `SecurityConfig.java` (Spring Security 설정)
- [ ] 3.7 `WebConfig.java` (CORS, 정적 파일 서빙)

### Step 4: Auth 모듈 생성 (Story: C1-1, A1-1)
- [ ] 4.1 Auth DTO 클래스 (Request/Response)
- [ ] 4.2 `AuthService.java` (로그인 로직, 시도 제한, 토큰 발급)
- [ ] 4.3 `AuthController.java` (POST /api/auth/admin/login, POST /api/auth/table/login)

### Step 5: Menu 모듈 생성 (Story: C2-1, A4-1, A4-2, A4-3)
- [ ] 5.1 Menu/Category DTO 클래스
- [ ] 5.2 `FileStorageService.java` (이미지 업로드/삭제)
- [ ] 5.3 `MenuService.java` (CRUD, 순서 조정)
- [ ] 5.4 `MenuController.java` (GET /api/menus, /api/categories, POST/PUT/DELETE /api/admin/menus)

### Step 6: SSE 모듈 생성 (Story: C5-2, A2-1)
- [ ] 6.1 `SseService.java` (Emitter 풀 관리, 이벤트 발행)
- [ ] 6.2 `SseController.java` (GET /api/sse/orders, GET /api/admin/sse/orders)

### Step 7: Order 모듈 생성 (Story: C4-1, C5-1, A2-2, A3-2)
- [ ] 7.1 Order DTO 클래스
- [ ] 7.2 `OrderService.java` (주문 생성, 상태 변경, 삭제, SSE 발행)
- [ ] 7.3 `OrderController.java` (POST /api/orders, GET /api/orders/session, PUT/DELETE /api/admin/orders/**)

### Step 8: Table 모듈 생성 (Story: A3-1, A3-3, A3-4)
- [ ] 8.1 Table DTO 클래스
- [ ] 8.2 `TableService.java` (초기 설정, 이용 완료, 과거 내역)
- [ ] 8.3 `TableController.java` (GET/POST /api/admin/tables/**)

### Step 9: 검증
- [ ] 9.1 컴파일 확인 (`mvn compile`)
- [ ] 9.2 Docker 빌드 확인 (`docker build`)
