# Component Methods - 테이블오더 서비스

> 상세 비즈니스 로직은 Construction Phase의 Functional Design에서 정의됩니다.
> 여기서는 메서드 시그니처와 고수준 목적만 정의합니다.

---

## Backend - Controller Methods

### AuthController

```java
// 관리자 로그인
POST /api/auth/admin/login
AdminLoginResponse login(AdminLoginRequest request)

// 테이블 자동 로그인
POST /api/auth/table/login
TableLoginResponse tableLogin(TableLoginRequest request)

// 관리자 로그아웃
POST /api/auth/admin/logout
void logout(HttpServletRequest request)
```

### MenuController

```java
// 카테고리 목록 조회 (공용)
GET /api/categories
List<CategoryResponse> getCategories()

// 카테고리별 메뉴 조회 (공용)
GET /api/menus?categoryId={id}
List<MenuResponse> getMenusByCategory(Long categoryId)

// 메뉴 등록 (관리자)
POST /api/admin/menus
MenuResponse createMenu(MenuCreateRequest request, MultipartFile image)

// 메뉴 수정 (관리자)
PUT /api/admin/menus/{menuId}
MenuResponse updateMenu(Long menuId, MenuUpdateRequest request, MultipartFile image)

// 메뉴 삭제 (관리자)
DELETE /api/admin/menus/{menuId}
void deleteMenu(Long menuId)

// 메뉴 순서 조정 (관리자)
PUT /api/admin/menus/order
void updateMenuOrder(List<MenuOrderRequest> orderList)
```

### OrderController

```java
// 주문 생성 (고객)
POST /api/orders
OrderResponse createOrder(OrderCreateRequest request)

// 현재 세션 주문 내역 조회 (고객)
GET /api/orders/session
List<OrderResponse> getSessionOrders()

// 테이블별 주문 목록 조회 (관리자)
GET /api/admin/tables/{tableId}/orders
List<OrderResponse> getTableOrders(Long tableId)

// 주문 상태 변경 (관리자)
PUT /api/admin/orders/{orderId}/status
OrderResponse updateOrderStatus(Long orderId, OrderStatusRequest request)

// 주문 삭제 (관리자)
DELETE /api/admin/orders/{orderId}
void deleteOrder(Long orderId)
```

### TableController

```java
// 테이블 목록 조회 (관리자)
GET /api/admin/tables
List<TableResponse> getTables()

// 테이블 초기 설정 (관리자)
POST /api/admin/tables/{tableId}/setup
TableSetupResponse setupTable(Long tableId, TableSetupRequest request)

// 테이블 이용 완료 처리 (관리자)
POST /api/admin/tables/{tableId}/complete
void completeTableSession(Long tableId)

// 과거 주문 내역 조회 (관리자)
GET /api/admin/tables/{tableId}/history
List<OrderHistoryResponse> getOrderHistory(Long tableId, LocalDate from, LocalDate to)
```

### SseController

```java
// 관리자용 SSE 스트림
GET /api/admin/sse/orders
SseEmitter subscribeAdminOrders()

// 고객용 SSE 스트림 (테이블 세션별)
GET /api/sse/orders
SseEmitter subscribeTableOrders()
```

---

## Backend - Service Methods

### AuthService

```java
// 관리자 자격증명 검증 및 JWT 발급
AdminLoginResponse authenticateAdmin(String storeId, String username, String password)

// 테이블 세션 토큰 발급
TableLoginResponse authenticateTable(String storeId, int tableNumber, String password)

// JWT 토큰 생성
String generateAdminToken(Long adminId, String storeId)

// 테이블 세션 토큰 생성
String generateTableToken(Long tableId, Long sessionId)

// 토큰 검증 및 파싱
Claims parseToken(String token)

// 로그인 시도 횟수 확인/증가/초기화
void checkLoginAttempts(String storeId, String username)
void incrementLoginAttempts(String storeId, String username)
void resetLoginAttempts(String storeId, String username)
```

### MenuService

```java
// 카테고리 목록 조회
List<CategoryResponse> getAllCategories(Long storeId)

// 카테고리별 메뉴 조회
List<MenuResponse> getMenusByCategory(Long storeId, Long categoryId)

// 메뉴 생성 (이미지 업로드 포함)
MenuResponse createMenu(Long storeId, MenuCreateRequest request, MultipartFile image)

// 메뉴 수정
MenuResponse updateMenu(Long storeId, Long menuId, MenuUpdateRequest request, MultipartFile image)

// 메뉴 삭제
void deleteMenu(Long storeId, Long menuId)

// 메뉴 순서 업데이트
void updateMenuOrder(Long storeId, List<MenuOrderRequest> orderList)

// 이미지 파일 저장
String saveImageFile(MultipartFile file)

// 이미지 파일 삭제
void deleteImageFile(String filePath)
```

### OrderService

```java
// 주문 생성
OrderResponse createOrder(Long tableId, Long sessionId, List<OrderItemRequest> items)

// 현재 세션 주문 조회
List<OrderResponse> getOrdersBySession(Long sessionId)

// 테이블 현재 주문 조회 (관리자)
List<OrderResponse> getActiveOrdersByTable(Long tableId)

// 주문 상태 변경
OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus)

// 주문 삭제
void deleteOrder(Long orderId)

// SSE 이벤트 발행 (신규 주문)
void publishNewOrderEvent(Long storeId, OrderResponse order)

// SSE 이벤트 발행 (상태 변경)
void publishOrderStatusEvent(Long sessionId, Long orderId, OrderStatus status)
```

### TableService

```java
// 테이블 목록 조회
List<TableResponse> getTablesByStore(Long storeId)

// 테이블 초기 설정
TableSetupResponse setupTable(Long storeId, Long tableId, String password)

// 테이블 세션 생성
TableSession createSession(Long tableId)

// 테이블 이용 완료 처리
void completeSession(Long storeId, Long tableId)

// 과거 주문 이력 조회
List<OrderHistoryResponse> getOrderHistory(Long tableId, LocalDate from, LocalDate to)
```

### SseService

```java
// 관리자 SSE Emitter 등록
SseEmitter createAdminEmitter(Long storeId)

// 고객 SSE Emitter 등록
SseEmitter createTableEmitter(Long sessionId)

// 관리자에게 이벤트 전송
void sendToAdmin(Long storeId, String eventName, Object data)

// 고객(테이블 세션)에게 이벤트 전송
void sendToTable(Long sessionId, String eventName, Object data)

// Emitter 제거 (연결 종료 시)
void removeEmitter(SseEmitter emitter)
```

---

## Frontend - Customer App

### useCart Hook

```typescript
interface CartItem {
  menuId: number;
  menuName: string;
  price: number;
  quantity: number;
  imageUrl?: string;
}

const useCart = () => {
  addItem(menu: MenuResponse): void
  removeItem(menuId: number): void
  updateQuantity(menuId: number, quantity: number): void
  clearCart(): void
  items: CartItem[]
  totalAmount: number
  totalCount: number
}
```

### useOrderSse Hook

```typescript
const useOrderSse = (sessionId: number) => {
  orders: OrderResponse[]           // 실시간 업데이트되는 주문 목록
  connectionStatus: 'connected' | 'reconnecting' | 'disconnected'
  connect(): void
  disconnect(): void
}
```

### AutoLoginGuard

```typescript
const AutoLoginGuard = ({ children }: Props) => {
  // localStorage에서 인증 정보 로드 후 자동 로그인 시도
  // 성공: children 렌더링
  // 실패: SetupPage로 리다이렉트
}
```

---

## Frontend - Admin App

### useAdminSse Hook

```typescript
const useAdminSse = (storeId: number) => {
  newOrder: OrderResponse | null      // 최신 신규 주문
  updatedOrder: OrderResponse | null  // 상태 변경된 주문
  connectionStatus: 'connected' | 'reconnecting' | 'disconnected'
  connect(): void
  disconnect(): void
}
```

### useAuth Hook

```typescript
const useAuth = () => {
  isAuthenticated: boolean
  adminInfo: AdminInfo | null
  login(storeId: string, username: string, password: string): Promise<void>
  logout(): void
  getToken(): string | null
}
```

### API Client (axios instance)

```typescript
// JWT 자동 첨부 interceptor
const adminApiClient = axios.create({ baseURL: '/api' })
adminApiClient.interceptors.request.use(attachJwtToken)
adminApiClient.interceptors.response.use(handleAuthError)

// 테이블 세션 토큰 자동 첨부 interceptor
const customerApiClient = axios.create({ baseURL: '/api' })
customerApiClient.interceptors.request.use(attachTableToken)
```
