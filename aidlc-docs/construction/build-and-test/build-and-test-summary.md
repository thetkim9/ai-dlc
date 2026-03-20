# Build and Test Summary

## Unit 1: Backend (Spring Boot)

### 빌드 방법

```bash
# 루트에서 backend만 빌드
cd backend
mvn package -DskipTests

# Docker 이미지 빌드
docker-compose build backend
```

### 단위 테스트 실행

```bash
cd backend
mvn test
```

**테스트 결과**: 전체 통과 (Java 21 환경)

| 테스트 클래스 | 테스트 수 | 상태 |
|-------------|---------|------|
| AuthServiceTest | 7 | PASS |
| AuthControllerTest | 3 | PASS |
| MenuServiceTest | 6 | PASS |
| FileStorageServiceTest | 3 | PASS |
| OrderServiceTest | 7 | PASS |
| OrderControllerTest | 3 | PASS |
| TableServiceTest | 5 | PASS |

### 기능 테스트 (Docker Compose)

**사전 조건**:
1. `.env` 파일 생성 (루트)
2. Docker 설치 및 실행

```bash
# backend + postgres만 기동
docker-compose up --build postgres backend

# 전체 기동 (frontend 포함)
docker-compose up --build
```

**기능 테스트 순서**:

1. 헬스체크 확인
```
GET http://localhost:8080/actuator/health
```

2. 관리자 로그인
```
POST http://localhost:8080/api/auth/admin/login
{ "storeCode": "STORE001", "username": "admin", "password": "admin1234" }
```

3. 테이블 초기 설정 (관리자 토큰 필요)
```
POST http://localhost:8080/api/admin/tables/1/setup
Authorization: Bearer {admin_token}
{ "password": "1234" }
```

4. 메뉴 조회
```
GET http://localhost:8080/api/categories?storeId=1
GET http://localhost:8080/api/menus?storeId=1&categoryId=1
```

5. 테이블 로그인
```
POST http://localhost:8080/api/auth/table/login
{ "storeCode": "STORE001", "tableNumber": 1, "password": "1234" }
```

6. 주문 생성 (테이블 토큰 필요)
```
POST http://localhost:8080/api/orders
Authorization: Bearer {table_token}
{ "items": [{ "menuId": 1, "quantity": 2 }] }
```

7. 주문 상태 변경 (관리자 토큰 필요)
```
PUT http://localhost:8080/api/admin/orders/1/status
Authorization: Bearer {admin_token}
{ "status": "PREPARING" }
```

8. 이용 완료 처리
```
POST http://localhost:8080/api/admin/tables/1/complete
Authorization: Bearer {admin_token}
```

---

## Unit 2: Frontend Customer (React)

### 빌드 방법

```bash
cd frontend/customer
npm install
npm run build

# Docker 빌드
docker-compose build customer-app
```

### 개발 서버 실행

```bash
cd frontend/customer
npm run dev
# http://localhost:5173
```

---

## Unit 3: Frontend Admin (React)

### 빌드 방법

```bash
cd frontend/admin
npm install
npm run build

# Docker 빌드
docker-compose build admin-app
```

### 개발 서버 실행

```bash
cd frontend/admin
npm run dev
# http://localhost:5174
```

---

## 전체 통합 실행

```bash
# 루트 디렉토리에서
docker-compose up --build

# 접속
# 고객 앱: http://localhost:3000
# 관리자 앱: http://localhost:3001
# Backend API: http://localhost:8080
```

## 환경 변수 설정 (.env)

```env
POSTGRES_USER=tableorder
POSTGRES_PASSWORD=tableorder123
JWT_SECRET=tableorder-jwt-secret-key-for-development-minimum-256-bits-long
```

## 주요 이슈 및 해결

| 이슈 | 해결 방법 |
|------|---------|
| Lombok annotation processor 미동작 | 모든 파일에서 Lombok 제거, 명시적 코드로 대체 |
| flyway-database-postgresql version 누락 | pom.xml에 버전 명시 (10.10.0) |
| JDK 26 컴파일 에러 | JDK 21 LTS로 변경 |
