# Unit of Work Dependency - 테이블오더 서비스

## 의존성 매트릭스

| Unit | 의존 대상 | 의존 유형 | 설명 |
|------|---------|---------|------|
| Unit 2: Frontend-Customer | Unit 1: Backend | Runtime (HTTP/SSE) | REST API 호출, SSE 연결 |
| Unit 3: Frontend-Admin | Unit 1: Backend | Runtime (HTTP/SSE) | REST API 호출, SSE 연결 |
| Unit 1: Backend | PostgreSQL | Runtime (JDBC) | 데이터 영속성 |
| Unit 2: Frontend-Customer | Unit 3: Frontend-Admin | 없음 | 독립적 |

## 개발 순서 권장

```
1단계: Unit 1 (Backend)
    - 모든 API 엔드포인트 구현
    - SSE 스트림 구현
    - DB 스키마 확정
    - Docker 컨테이너 실행 가능 상태

2단계: Unit 2 + Unit 3 (병렬 가능)
    - Backend API 계약 기반으로 독립 개발
    - Mock API 또는 실제 Backend 연동하여 개발
```

## 통합 포인트

| 통합 포인트 | Unit 2 (Customer) | Unit 3 (Admin) |
|-----------|------------------|----------------|
| 인증 API | POST /api/auth/table/login | POST /api/auth/admin/login |
| 메뉴 조회 | GET /api/menus | GET /api/admin/menus |
| 주문 생성 | POST /api/orders | - |
| 주문 조회 | GET /api/orders/session | GET /api/admin/tables/{id}/orders |
| 주문 상태 변경 | - (수신만) | PUT /api/admin/orders/{id}/status |
| SSE 연결 | GET /api/sse/orders | GET /api/admin/sse/orders |
| 테이블 관리 | - | POST /api/admin/tables/{id}/setup |
| 메뉴 관리 | - | POST/PUT/DELETE /api/admin/menus |

## 환경 변수 공유

```
# .env (공통)
POSTGRES_DB=tableorder
POSTGRES_USER=tableorder
POSTGRES_PASSWORD=<secret>
JWT_SECRET=<secret>
JWT_EXPIRY_HOURS=16

# Backend
BACKEND_PORT=8080
UPLOAD_DIR=/app/uploads

# Frontend (빌드 시 주입)
VITE_API_BASE_URL=http://localhost:8080
```
