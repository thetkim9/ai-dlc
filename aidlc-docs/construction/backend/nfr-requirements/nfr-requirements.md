# NFR Requirements - Backend (Unit 1)

## 성능 (Performance)

| ID | 요구사항 | 목표값 |
|----|---------|--------|
| NFR-P-01 | SSE 주문 업데이트 지연 | 2초 이내 |
| NFR-P-02 | 메뉴 조회 API 응답 | 1초 이내 (p95) |
| NFR-P-03 | 주문 생성 API 응답 | 2초 이내 (p95) |
| NFR-P-04 | 동시 접속 처리 | 테이블 10개 + 관리자 1명 |

소규모 기준이므로 별도 캐싱 레이어 불필요. Spring Boot 기본 설정으로 충분.

## 보안 (Security)

| ID | 요구사항 | 구현 방법 |
|----|---------|---------|
| NFR-S-01 | 관리자 인증 | JWT (HS256, 16시간 만료) |
| NFR-S-02 | 비밀번호 저장 | BCryptPasswordEncoder (strength 10) |
| NFR-S-03 | 로그인 시도 제한 | In-memory ConcurrentHashMap, 5회 실패 시 잠금 |
| NFR-S-04 | 테이블 세션 인증 | JWT (role=TABLE claim) |
| NFR-S-05 | 민감 정보 관리 | 환경 변수 (.env), 코드 하드코딩 금지 |
| NFR-S-06 | CORS 설정 | 프론트엔드 origin만 허용 |
| NFR-S-07 | SQL Injection 방지 | Spring Data JPA (PreparedStatement) |

## 가용성 (Availability)

- 로컬 Docker 환경 기준, 고가용성 불필요
- 단일 인스턴스 운영
- Docker Compose `restart: unless-stopped` 정책으로 자동 재시작

## 유지보수성 (Maintainability)

| ID | 요구사항 |
|----|---------|
| NFR-M-01 | 표준 Spring Boot 레이어 구조 (Controller / Service / Repository) |
| NFR-M-02 | 환경별 설정 분리 (application.yml) |
| NFR-M-03 | Flyway DB 마이그레이션으로 스키마 버전 관리 |
| NFR-M-04 | 표준 에러 응답 형식 (GlobalExceptionHandler) |
| NFR-M-05 | Lombok 미사용 - 명시적 생성자/getter 작성 |

## 운영 (Operations)

| ID | 요구사항 |
|----|---------|
| NFR-O-01 | Docker 컨테이너로 실행 가능 |
| NFR-O-02 | 환경 변수로 모든 설정 주입 가능 |
| NFR-O-03 | 이미지 파일 Docker volume 마운트 |
| NFR-O-04 | 헬스체크 엔드포인트 (GET /actuator/health) |

## 기술 스택 결정

| 항목 | 선택 | 이유 |
|------|------|------|
| Language | Java 21 (LTS) | 안정성, Spring Boot 3.x 호환 |
| Framework | Spring Boot 3.4.x | 표준, 생산성 |
| DB | PostgreSQL 15 | 안정성, Docker 지원 |
| ORM | Spring Data JPA + Hibernate | 표준 |
| Migration | Flyway | 스키마 버전 관리 |
| Auth | JWT (jjwt 0.12.x) | Stateless, 소규모 적합 |
| Realtime | SSE | WebSocket 대비 단순, 단방향 충분 |
| Build | Maven | 표준 |
| Container | Docker + Docker Compose | 로컬 배포 |
