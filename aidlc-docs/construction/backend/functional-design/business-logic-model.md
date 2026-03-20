# Business Logic Model - Backend (Unit 1)

## 핵심 비즈니스 플로우

### 1. 관리자 로그인 플로우

```
입력: storeCode, username, password
  |
  v
1. 로그인 시도 횟수 확인 (5회 초과 시 429)
  |
  v
2. Store 조회 (storeCode) → 없으면 401
  |
  v
3. StoreAdmin 조회 (store + username) → 없으면 401
  |
  v
4. password bcrypt 검증 → 실패 시 401 + 시도 횟수 증가
  |
  v
5. 시도 횟수 초기화
  |
  v
6. JWT 생성 (adminId, storeId, role=ADMIN, 만료 16시간)
  |
  v
출력: { token, adminId, storeId }
```

### 2. 테이블 로그인 플로우

```
입력: storeCode, tableNumber, password
  |
  v
1. Store 조회 (storeCode) → 없으면 401
  |
  v
2. TableEntity 조회 (store + tableNumber) → 없으면 401
  |
  v
3. password bcrypt 검증 → 실패 시 401
  |
  v
4. TableSession 조회 (table + ACTIVE)
   → 없으면 401 (관리자 초기 설정 필요)
  |
  v
5. 테이블 세션 토큰 생성 (tableId, sessionId, storeId, role=TABLE)
  |
  v
출력: { token, tableId, sessionId, tableNumber }
```

### 3. 주문 생성 플로우

```
입력: [{ menuId, quantity }] (테이블 세션 토큰 인증 후)
  |
  v
1. 토큰에서 tableId, sessionId, storeId 추출
  |
  v
2. TableSession 만료 확인 (expiresAt < now → 403)
  |
  v
3. 주문 항목 비어있으면 400
  |
  v
4. 각 menuId 유효성 확인 (해당 store 메뉴 + available=true)
  |
  v
5. OrderItem 생성 (menuName/unitPrice 스냅샷)
  |
  v
6. totalAmount = sum(quantity * unitPrice)
  |
  v
7. Order 저장 (status=PENDING, isHistory=false)
  |
  v
8. SSE 이벤트 발행 → 관리자 채널 (storeId 기준, "new-order")
  |
  v
출력: OrderResponse { orderId, items, totalAmount, status }
```

### 4. 주문 상태 변경 플로우

```
입력: orderId, newStatus (관리자 JWT 인증 후)
  |
  v
1. Order 조회 + store 소유권 확인
  |
  v
2. 상태 전이 유효성 확인
   PENDING → PREPARING: OK
   PREPARING → COMPLETED: OK
   그 외: 400 Bad Request
  |
  v
3. Order.status 업데이트
   COMPLETED 시 completedAt = now()
  |
  v
4. SSE 이벤트 발행:
   - 관리자 채널: "order-status-changed"
   - 고객 채널 (sessionId 기준): "order-status-changed"
  |
  v
출력: OrderResponse (업데이트된 상태)
```

### 5. 테이블 초기 설정 플로우

```
입력: tableId, password (관리자 JWT 인증 후)
  |
  v
1. Store 소유권 확인
  |
  v
2. 기존 ACTIVE 세션 있으면 COMPLETED 처리
  |
  v
3. 테이블 비밀번호 bcrypt 해시 후 저장
  |
  v
4. 새 TableSession 생성 (ACTIVE, expiresAt = now + 16시간)
  |
  v
출력: { tableId, sessionId, tableNumber, expiresAt }
```

### 6. 이용 완료 플로우

```
입력: tableId (관리자 JWT 인증 후)
  |
  v
1. ACTIVE TableSession 조회 → 없으면 404
  |
  v
2. 해당 세션의 모든 Order.isHistory = true 일괄 업데이트
  |
  v
3. TableSession.status = COMPLETED, completedAt = now()
  |
  v
4. SSE 이벤트 발행 → 관리자 채널 ("table-reset")
  |
  v
출력: 204 No Content
```

### 7. 메뉴 이미지 업로드 플로우

```
입력: MultipartFile image
  |
  v
1. 파일 확장자 검증 (jpg/jpeg/png/webp만 허용)
  |
  v
2. 파일 크기 검증 (최대 5MB)
  |
  v
3. UUID 기반 파일명 생성: {UUID}.{ext}
  |
  v
4. 저장 경로: {UPLOAD_DIR}/{storeId}/{filename}
  |
  v
5. 디렉토리 없으면 생성 후 파일 저장
  |
  v
출력: "/uploads/{storeId}/{filename}" (접근 URL)
```

---

## 데이터 접근 패턴

### 관리자 테이블 목록 조회

```
GET /api/admin/tables
  |
  v
1. store의 모든 TableEntity 조회
  |
  v
2. 각 테이블의 ACTIVE 세션 조회 (Optional)
  |
  v
출력: [{ tableId, tableNumber, sessionId, sessionStatus, expiresAt }]
```

소규모(테이블 10개 이하) 기준이므로 N+1 쿼리 허용.

---

## 에러 처리 표준

| 상황 | HTTP 상태 |
|------|---------|
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 유효성 검증 실패 | 400 Bad Request |
| 로그인 시도 초과 | 429 Too Many Requests |
| 잘못된 상태 전이 | 400 Bad Request |
| 서버 오류 | 500 Internal Server Error |

```json
// 표준 에러 응답 형식
{
  "error": "에러 메시지"
}
```
