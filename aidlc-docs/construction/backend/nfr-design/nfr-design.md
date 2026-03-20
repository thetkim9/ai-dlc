# NFR Design - Backend (Unit 1)

## 보안 설계

### JWT 인증 필터 체인

```
HTTP Request
    |
    v
JwtAuthenticationFilter (OncePerRequestFilter)
    ├── Authorization 헤더 추출 (Bearer {token})
    ├── jjwt로 JWT 파싱 및 서명 검증
    ├── role claim 확인 (ADMIN / TABLE)
    ├── ADMIN → AdminPrincipal(adminId, storeId) 생성
    ├── TABLE → TablePrincipal(tableId, sessionId, storeId) 생성
    └── SecurityContextHolder에 Authentication 설정
    |
    v
Spring Security Authorization
    ├── /api/admin/**          → ROLE_ADMIN 필요
    ├── /api/orders/**         → ROLE_TABLE 필요
    ├── /api/sse/orders        → ROLE_TABLE 필요
    ├── /api/auth/**           → 공개
    ├── GET /api/categories    → 공개
    ├── GET /api/menus         → 공개
    └── /uploads/**            → 공개
```

### 로그인 시도 제한 패턴

```java
// In-memory 구현 (소규모 기준, 재시작 시 초기화 허용)
Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

// 키: "{storeCode}:{username}"
// 5회 초과 시 429 Too Many Requests
// 로그인 성공 시 카운터 제거
```

### Principal 설계

```java
// 관리자 Principal
class AdminPrincipal {
    Long adminId;
    Long storeId;
}

// 테이블 Principal
class TablePrincipal {
    Long tableId;
    Long sessionId;
    Long storeId;
}
```

Controller에서 `@AuthenticationPrincipal`로 주입받아 storeId 기반 소유권 검증.

---

## SSE 설계

### Emitter 풀 관리

```java
// 관리자용: storeId → List<SseEmitter>
Map<Long, List<SseEmitter>> adminEmitters = new ConcurrentHashMap<>();

// 고객용: sessionId → List<SseEmitter>
Map<Long, List<SseEmitter>> tableEmitters = new ConcurrentHashMap<>();
```

### Emitter 생명주기

```
연결 요청
    |
    v
SseEmitter 생성 (timeout=30분)
    |
    v
onCompletion / onTimeout / onError 핸들러 등록
    → 각 이벤트 발생 시 Emitter 풀에서 제거
    |
    v
초기 "connected" 이벤트 전송
    |
    v
이벤트 발행 (new-order, order-status-changed, table-reset 등)
    |
    v
연결 종료 → 풀에서 제거
```

### 이벤트 발행 패턴

```java
// 발행 실패 시 해당 Emitter 제거 (클라이언트 재연결 유도)
private void sendEvent(SseEmitter emitter, String eventName, Object data) {
    try {
        String json = objectMapper.writeValueAsString(data);
        emitter.send(SseEmitter.event().name(eventName).data(json));
    } catch (IOException e) {
        emitter.completeWithError(e);
        throw new RuntimeException("SSE 이벤트 전송 실패", e);
    }
}
```

---

## 에러 처리 설계

### GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // ApiException → 정의된 HTTP 상태 코드 반환
    // MethodArgumentNotValidException → 400 (Bean Validation 실패)
    // Exception → 500 (예상치 못한 오류)
}

// 응답 형식
{ "error": "에러 메시지" }
```

### ApiException 팩토리 메서드

```java
ApiException.notFound("메뉴를 찾을 수 없습니다.")      // 404
ApiException.badRequest("가격은 0보다 커야 합니다.")    // 400
ApiException.unauthorized("인증이 필요합니다.")         // 401
ApiException.forbidden("세션이 만료되었습니다.")        // 403
ApiException.tooManyRequests("로그인 시도 초과")        // 429
```

---

## 파일 업로드 설계

### 안전한 파일 저장

```java
// 원본 파일명 사용 금지 → UUID 기반 파일명
String ext = getExtension(originalFilename);  // jpg/png/webp
String savedName = UUID.randomUUID() + "." + ext;
Path savePath = Paths.get(uploadDir, storeId.toString(), savedName);
Files.createDirectories(savePath.getParent());
Files.write(savePath, file.getBytes());
return "/uploads/" + storeId + "/" + savedName;
```

### 정적 파일 서빙

```java
// WebConfig: /uploads/** → Docker volume 경로 매핑
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir + "/");
```

---

## 트랜잭션 설계

| 메서드 | 트랜잭션 설정 |
|--------|-------------|
| 조회 메서드 | `@Transactional(readOnly = true)` |
| 생성/수정/삭제 | `@Transactional` |
| 이용 완료 처리 | `@Transactional` (주문 일괄 업데이트 + 세션 종료 원자적 처리) |

---

## CORS 설계

```java
// SecurityConfig에서 설정
config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);

// 환경 변수로 주입
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```
