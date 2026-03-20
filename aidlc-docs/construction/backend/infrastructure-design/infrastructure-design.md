# Infrastructure Design - Backend (Unit 1)

## 배포 환경

Docker Compose 기반 로컬 서버 배포

## 컨테이너 구성

### 전체 서비스 구성

```
docker-compose.yml
├── postgres      (PostgreSQL 15)
├── backend       (Spring Boot 3.4.x, port 8080)
├── customer-app  (React, port 3000) ← Unit 2
└── admin-app     (React, port 3001) ← Unit 3
```

### backend 컨테이너

```yaml
backend:
  build: ./backend
  ports:
    - "8080:8080"
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/tableorder
    SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
    JWT_EXPIRY_HOURS: 16
    UPLOAD_DIR: /app/uploads
    CORS_ALLOWED_ORIGINS: http://localhost:3000,http://localhost:3001
  volumes:
    - uploads_data:/app/uploads
  depends_on:
    postgres:
      condition: service_healthy
  restart: unless-stopped
```

### postgres 컨테이너

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: tableorder
    POSTGRES_USER: ${POSTGRES_USER}
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
  volumes:
    - postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
    interval: 5s
    timeout: 5s
    retries: 5
  restart: unless-stopped
```

## Dockerfile (Backend)

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 볼륨 구성

```yaml
volumes:
  postgres_data:    # PostgreSQL 데이터 영속성
  uploads_data:     # 메뉴 이미지 파일 영속성
```

## 네트워크

```yaml
networks:
  tableorder-network:
    driver: bridge
```

모든 컨테이너가 동일 네트워크에서 서비스명으로 통신 (예: `postgres:5432`)

## 환경 변수 (.env)

```env
# PostgreSQL
POSTGRES_USER=tableorder
POSTGRES_PASSWORD=your_secure_password

# JWT (최소 256비트 이상)
JWT_SECRET=your-very-long-and-secure-jwt-secret-key-at-least-256-bits-long
```

## DB 마이그레이션

Flyway 자동 실행 (애플리케이션 시작 시):
- `V1__init_schema.sql` - 테이블 생성
- `V2__seed_data.sql` - 초기 데이터 (매장, 관리자, 테이블, 카테고리)

## 정적 파일 서빙

Spring Boot에서 `/uploads/**` 경로를 Docker volume 마운트 경로로 매핑:

```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");
```

## 기동 순서

```
1. postgres 기동 (healthcheck 통과까지 대기)
2. backend 기동 (Flyway 마이그레이션 자동 실행)
3. customer-app, admin-app 기동
```

## 포트 구성

| 서비스 | 내부 포트 | 외부 포트 |
|--------|---------|---------|
| backend | 8080 | 8080 |
| customer-app | 80 | 3000 |
| admin-app | 80 | 3001 |
| postgres | 5432 | (미노출) |
