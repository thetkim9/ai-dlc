# TDD Code Generation Plan - Backend (Unit 1)

## Unit Context
- **Workspace Root**: `backend/` (모노레포 루트 기준)
- **Project Type**: Greenfield
- **Stories**: C1-1, C2-1, C4-1, C5-1, C5-2, A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3
- **Test Framework**: JUnit 5 + Mockito + Spring Boot Test (@WebMvcTest, @DataJpaTest)

---

## Plan Step 0: 프로젝트 구조 및 Contract Skeleton 생성
- [x] 0.1 `backend/pom.xml` 생성
- [x] 0.2 `backend/src/main/resources/application.yml` 생성
- [x] 0.3 `backend/src/main/java/com/tableorder/TableOrderApplication.java` 생성
- [x] 0.4 Entity 클래스 전체 생성 (Store, StoreAdmin, TableEntity, TableSession, Category, Menu, Order, OrderItem)
- [x] 0.5 Repository 인터페이스 전체 생성 (메서드 시그니처만)
- [x] 0.6 DTO 클래스 전체 생성 (Request/Response)
- [x] 0.7 Service 클래스 skeleton 생성 (모든 메서드 `throw new UnsupportedOperationException()`)
- [x] 0.8 Controller 클래스 skeleton 생성
- [x] 0.9 공통 컴포넌트 skeleton (Exception, GlobalExceptionHandler, JwtUtil, SecurityConfig)
- [x] 0.10 컴파일 확인 (getDiagnostics - 오류 없음, Maven 미설치 환경)
- [x] 0.11 Flyway 마이그레이션 스크립트 생성 (V1__init_schema.sql, V2__seed_data.sql)
- [x] 0.12 `backend/Dockerfile` 생성
- [x] 0.13 루트 `docker-compose.yml` 생성
- [x] 0.14 루트 `.env.example` 생성

---

## Plan Step 1: AuthService TDD (Story: A1-1, C1-1)

- [x] AuthService.authenticateAdmin() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-001 (유효한 자격증명 로그인 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-002 (잘못된 비밀번호)
  - [x] GREEN: 비밀번호 검증 추가
  - [x] RED: TC-BE-003 (존재하지 않는 매장)
  - [x] GREEN: 매장 조회 추가
  - [x] RED: TC-BE-004 (5회 실패 후 잠금)
  - [x] GREEN: 시도 횟수 제한 추가
  - [x] REFACTOR: 코드 정리
  - [x] VERIFY: 모든 테스트 통과

- [x] AuthService.authenticateTable() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-005 (테이블 로그인 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-006 (ACTIVE 세션 없음)
  - [x] GREEN: 세션 검증 추가
  - [x] REFACTOR + VERIFY

- [x] JwtUtil (generateToken, parseToken) - RED-GREEN-REFACTOR
  - [x] RED: 토큰 생성 및 파싱 테스트
  - [x] GREEN: jjwt 구현
  - [x] RED: 만료 토큰 검증 테스트
  - [x] GREEN: 만료 검증 추가
  - [x] REFACTOR + VERIFY

---

## Plan Step 2: MenuService TDD (Story: A4-1, A4-2, A4-3, C2-1)

- [x] FileStorageService - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-023 (유효한 파일 저장)
  - [x] GREEN: 파일 저장 구현
  - [x] RED: TC-BE-024 (허용되지 않는 확장자)
  - [x] GREEN: 확장자 검증 추가
  - [x] RED: TC-BE-025 (크기 초과)
  - [x] GREEN: 크기 검증 추가
  - [x] REFACTOR + VERIFY

- [x] MenuService.createMenu() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-007 (메뉴 등록 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-008 (가격 0 이하)
  - [x] GREEN: 가격 검증 추가
  - [x] RED: TC-BE-009 (메뉴명 누락)
  - [x] GREEN: 필수 필드 검증 추가
  - [x] REFACTOR + VERIFY

- [x] MenuService.deleteMenu() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-010 (삭제 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-011 (존재하지 않는 메뉴)
  - [x] GREEN: 존재 검증 추가
  - [x] REFACTOR + VERIFY

- [x] MenuService.getCategoriesByStore(), getMenusByCategory(), updateMenuOrder() - RED-GREEN-REFACTOR

---

## Plan Step 3: OrderService TDD (Story: C4-1, C5-1, C5-2, A2-2, A3-2)

- [x] OrderService.createOrder() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-012 (주문 생성 성공 + SSE 발행 검증)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-013 (빈 주문)
  - [x] GREEN: 빈 주문 검증
  - [x] RED: TC-BE-014 (존재하지 않는 메뉴)
  - [x] GREEN: 메뉴 검증
  - [x] RED: TC-BE-015 (만료된 세션)
  - [x] GREEN: 세션 검증
  - [x] REFACTOR + VERIFY

- [x] OrderService.updateOrderStatus() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-016 (상태 변경 성공 + SSE 발행)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-017 (역방향 전이 실패)
  - [x] GREEN: 상태 전이 검증
  - [x] REFACTOR + VERIFY

- [x] OrderService.getOrdersBySession() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-018 (현재 세션 주문만 반환)
  - [x] GREEN + REFACTOR + VERIFY

- [x] OrderService.deleteOrder(), getActiveOrdersByTable() - RED-GREEN-REFACTOR

---

## Plan Step 4: TableService TDD (Story: A3-1, A3-3, A3-4)

- [x] TableService.setupTable() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-019 (신규 세션 생성)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-020 (기존 세션 종료 후 재생성)
  - [x] GREEN: 기존 세션 처리 추가
  - [x] REFACTOR + VERIFY

- [x] TableService.completeSession() - RED-GREEN-REFACTOR
  - [x] RED: TC-BE-021 (이용 완료 성공)
  - [x] GREEN: 최소 구현
  - [x] RED: TC-BE-022 (세션 없음)
  - [x] GREEN: 세션 검증
  - [x] REFACTOR + VERIFY

- [x] TableService.getTablesByStore(), getOrderHistory() - RED-GREEN-REFACTOR

---

## Plan Step 5: Security 및 Controller Integration Tests

- [x] JwtAuthenticationFilter 구현 및 테스트
- [x] SecurityConfig 설정 (공개/인증 엔드포인트 분리)
- [x] WebConfig 설정 (CORS, 정적 파일)
- [x] AuthController Integration Test (TC-BE-026~028)
- [x] OrderController Integration Test (TC-BE-029~031)
- [x] MenuController Integration Test (TC-BE-032~033)

---

## Plan Step 6: SseService 구현

- [x] SseService Emitter 풀 관리 구현
- [x] SseController 구현
- [x] SSE 이벤트 발행 검증 (OrderService, TableService 연동)

---

## Plan Step 7: 최종 검증

- [x] 전체 테스트 실행 (`mvn test`)
- [x] 컴파일 및 패키징 (`mvn package -DskipTests`)
- [x] Docker 빌드 확인
- [x] test-plan.md 모든 TC 🟢 확인
