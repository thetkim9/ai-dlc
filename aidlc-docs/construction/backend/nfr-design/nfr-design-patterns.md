# NFR Design Patterns - Backend (Unit 1)

## 보안 패턴

### JWT 인증 필터 체인
```
HTTP Request
    |
    v
JwtAuthenticationFilter (OncePerRequestFilter)
    ├── Authorization 헤더 추출
    ├── JWT 파싱 및 검증
    ├── role 확인 (ADMIN / TABLE)
    └── SecurityContext 설정
    |
    v
Spring Security Authorization
    ├── /api/admin/** → ADMIN role 필요
    ├── /api/orders, /api/sse/orders → TABLE role 필요
    └── /api/auth/**, /api/menus, /api/categories → 공개
```

### 로그인 시도 제한 패턴
```java
// In-memory 구현 (소규모 기준)
Map<String, LoginAttemptInfo> attemptCache = new ConcurrentHashMap<>();

class LoginAttemptInfo {
    int count;
    LocalDateTime lockedUntil; // null이면 잠금 없음
}
// 키: "{storeCode}:{username}"
```

## SSE 패턴

### Emitter 풀 관리
```java
// 관리자용: storeId → List<SseEmitter>
Map<Long, List<SseEmitter>> adminEmitters = new ConcurrentHashMap<>();

// 고객용: sessionId → List<SseEmitter>
Map<Long, List<SseEmitter>> tableEmitters = new ConcurrentHashMap<>();

// Emitter 등록 시 onCompletion/onTimeout/onError 핸들러 등록
emitter.onCompletion(() -> removeEmitter(emitter));
emitter.onTimeout(() -> removeEmitter(emitter));
emitter.onError(e -> removeEmitter(emitter));
```

### 이벤트 발행 패턴
```java
// 발행 실패 시 해당 Emitter 제거 (클라이언트 재연결 유도)
void sendEvent(SseEmitter emitter, String eventName, Object data) {
    try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(objectMapper.writeValueAsString(data)));
    } catch (Exception e) {
        removeEmitter(emitter);
    }
}
```

## 에러 처리 패턴

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // BusinessException → 400/401/403/404/429
    // MethodArgumentNotValidException → 400 (Bean Validation)
    // Exception → 500
}

// 표준 에러 응답
record ErrorResponse(String code, String message, String timestamp) {}
```

## 파일 업로드 패턴

### 안전한 파일 저장
```java
// 원본 파일명 사용 금지 → UUID 기반 파일명
String ext = getExtension(originalFilename); // jpg/png/webp
String savedName = UUID.randomUUID() + "." + ext;
Path savePath = Paths.get(uploadDir, storeId.toString(), savedName);
Files.createDirectories(savePath.getParent());
file.transferTo(savePath);
return "/uploads/" + storeId + "/" + savedName;
```

## 트랜잭션 패턴

- Service 메서드에 `@Transactional` 적용
- 읽기 전용 조회: `@Transactional(readOnly = true)`
- 이용 완료 처리 (주문 일괄 업데이트 + 세션 종료): 단일 트랜잭션
