# Logical Components - Backend (Unit 1)

## 컴포넌트 구성

| 컴포넌트 | 구현 방식 | 비고 |
|---------|---------|------|
| 인증 토큰 저장소 | JWT (stateless) | 별도 저장소 불필요 |
| 로그인 시도 카운터 | In-memory ConcurrentHashMap | 소규모, 재시작 시 초기화 허용 |
| SSE Emitter 풀 | In-memory ConcurrentHashMap | 재시작 시 클라이언트 재연결 |
| 이미지 파일 저장소 | 로컬 파일시스템 + Docker Volume | /app/uploads |
| 정적 파일 서빙 | Spring Boot WebMvcConfigurer | /uploads/** 경로 |
| DB 연결 풀 | HikariCP (Spring Boot 기본) | 기본 설정으로 충분 |
| DB 마이그레이션 | Flyway | 시작 시 자동 실행 |

## 레이어 구조

```
[HTTP Layer]
  Controller (요청/응답 변환, 입력 검증)
      |
[Business Layer]
  Service (@Transactional, 비즈니스 로직)
      |
[Data Layer]
  Repository (Spring Data JPA)
      |
[Infrastructure]
  PostgreSQL + File System
```

## 패키지 구조

```
com.tableorder/
├── common/
│   ├── exception/          # BusinessException, GlobalExceptionHandler
│   ├── response/           # ErrorResponse, ApiResponse
│   └── security/           # JwtFilter, SecurityConfig, JwtUtil
├── auth/
│   ├── AuthController
│   ├── AuthService
│   └── dto/
├── menu/
│   ├── MenuController
│   ├── MenuService
│   ├── FileStorageService  # 이미지 파일 처리
│   └── dto/
├── order/
│   ├── OrderController
│   ├── OrderService
│   └── dto/
├── table/
│   ├── TableController
│   ├── TableService
│   └── dto/
├── sse/
│   ├── SseController
│   └── SseService
└── entity/                 # JPA Entities
```
