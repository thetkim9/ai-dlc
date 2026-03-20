# Tech Stack Decisions - Backend (Unit 1)

## 확정된 기술 스택

| 항목 | 선택 | 버전 | 선택 이유 |
|------|------|------|---------|
| Language | Java | 17 (LTS) | 안정성, Spring Boot 3.x 지원 |
| Framework | Spring Boot | 3.2.x | 생산성, 자동 설정, 풍부한 생태계 |
| Security | Spring Security | 6.x | JWT 통합, 필터 체인 커스터마이징 |
| ORM | Spring Data JPA + Hibernate | 6.x | 표준 ORM, 쿼리 자동화 |
| DB | PostgreSQL | 15 | 안정성, JSON 지원, 소규모 적합 |
| DB Migration | Flyway | 9.x | 스키마 버전 관리 |
| JWT | jjwt (io.jsonwebtoken) | 0.12.x | 경량, Spring 통합 용이 |
| Build | Maven | 3.9.x | 표준 Java 빌드 도구 |
| Container | Docker | latest | 배포 일관성 |

## 주요 Spring Boot 의존성

```xml
<!-- pom.xml 주요 의존성 -->
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
spring-boot-starter-actuator
spring-boot-starter-validation
postgresql (runtime)
flyway-core
jjwt-api + jjwt-impl + jjwt-jackson
lombok
```

## SSE 구현 방식

Spring MVC의 `SseEmitter` 사용 (Spring WebFlux 불필요):
- 소규모 동시 접속 기준으로 MVC 방식으로 충분
- `SseEmitter` 풀을 `ConcurrentHashMap`으로 관리
- 타임아웃: 30분 (1800000ms)

## 파일 업로드 설정

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
```

## 환경 변수 목록

```
# DB
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/tableorder
SPRING_DATASOURCE_USERNAME=tableorder
SPRING_DATASOURCE_PASSWORD=<secret>

# JWT
JWT_SECRET=<min-256bit-secret>
JWT_EXPIRY_HOURS=16

# File Upload
UPLOAD_DIR=/app/uploads

# Server
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```
