# Contract/Interface Definition - Backend (Unit 1)

## Unit Context
- **Stories**: C1-1, C2-1, C4-1, C5-1, C5-2, A1-1, A2-1, A2-2, A3-1, A3-2, A3-3, A3-4, A4-1, A4-2, A4-3
- **Dependencies**: PostgreSQL, File System
- **Database Entities**: Store, StoreAdmin, TableEntity, TableSession, Category, Menu, Order, OrderItem

---

## Business Logic Layer

### AuthService
- `authenticateAdmin(storeCode, username, password) -> AdminLoginResponse`
  - Args: storeCode(String), username(String), password(String)
  - Returns: AdminLoginResponse { token, adminId, storeId }
  - Raises: UnauthorizedException, TooManyRequestsException

- `authenticateTable(storeCode, tableNumber, password) -> TableLoginResponse`
  - Args: storeCode(String), tableNumber(int), password(String)
  - Returns: TableLoginResponse { token, tableId, sessionId, tableNumber }
  - Raises: UnauthorizedException

- `checkLoginAttempts(key) -> void`
  - Args: key(String) = "{storeCode}:{username}"
  - Raises: TooManyRequestsException (5회 초과 + 잠금 중)

- `generateAdminToken(adminId, storeId) -> String`
- `generateTableToken(tableId, sessionId) -> String`
- `parseToken(token) -> Claims`
  - Raises: UnauthorizedException (만료/유효하지 않은 토큰)

### MenuService
- `getCategoriesByStore(storeId) -> List<CategoryResponse>`
- `getMenusByCategory(storeId, categoryId) -> List<MenuResponse>`
- `createMenu(storeId, request, imageFile) -> MenuResponse`
  - Raises: ValidationException (필수 필드 누락, 가격 <= 0)
- `updateMenu(storeId, menuId, request, imageFile) -> MenuResponse`
  - Raises: NotFoundException, ValidationException
- `deleteMenu(storeId, menuId) -> void`
  - Raises: NotFoundException
- `updateMenuOrder(storeId, orderList) -> void`

### FileStorageService
- `saveFile(storeId, file) -> String` (저장된 URL 반환)
  - Raises: ValidationException (허용되지 않는 확장자, 크기 초과)
- `deleteFile(fileUrl) -> void`

### OrderService
- `createOrder(tableId, sessionId, storeId, items) -> OrderResponse`
  - Raises: NotFoundException (메뉴 없음), ValidationException (빈 주문), ForbiddenException (세션 만료)
- `getOrdersBySession(sessionId) -> List<OrderResponse>`
- `getActiveOrdersByTable(storeId, tableId) -> List<OrderResponse>`
- `updateOrderStatus(storeId, orderId, newStatus) -> OrderResponse`
  - Raises: NotFoundException, BadRequestException (잘못된 상태 전이)
- `deleteOrder(storeId, orderId) -> void`
  - Raises: NotFoundException

### TableService
- `getTablesByStore(storeId) -> List<TableSummaryResponse>`
- `setupTable(storeId, tableId, password) -> TableSetupResponse`
  - Raises: NotFoundException
- `completeSession(storeId, tableId) -> void`
  - Raises: NotFoundException (활성 세션 없음)
- `getOrderHistory(storeId, tableId, from, to) -> List<OrderHistoryResponse>`

### SseService
- `createAdminEmitter(storeId) -> SseEmitter`
- `createTableEmitter(sessionId) -> SseEmitter`
- `sendToAdmin(storeId, eventName, data) -> void`
- `sendToTable(sessionId, eventName, data) -> void`

---

## API Layer

### AuthController
- `POST /api/auth/admin/login` → AdminLoginResponse (200)
- `POST /api/auth/table/login` → TableLoginResponse (200)

### MenuController
- `GET /api/categories` → List<CategoryResponse> (200) [공개]
- `GET /api/menus` → List<MenuResponse> (200) [공개, ?categoryId]
- `POST /api/admin/menus` → MenuResponse (201) [ADMIN]
- `PUT /api/admin/menus/{menuId}` → MenuResponse (200) [ADMIN]
- `DELETE /api/admin/menus/{menuId}` → 204 [ADMIN]
- `PUT /api/admin/menus/order` → 204 [ADMIN]

### OrderController
- `POST /api/orders` → OrderResponse (201) [TABLE]
- `GET /api/orders/session` → List<OrderResponse> (200) [TABLE]
- `GET /api/admin/tables/{tableId}/orders` → List<OrderResponse> (200) [ADMIN]
- `PUT /api/admin/orders/{orderId}/status` → OrderResponse (200) [ADMIN]
- `DELETE /api/admin/orders/{orderId}` → 204 [ADMIN]

### TableController
- `GET /api/admin/tables` → List<TableSummaryResponse> (200) [ADMIN]
- `POST /api/admin/tables/{tableId}/setup` → TableSetupResponse (200) [ADMIN]
- `POST /api/admin/tables/{tableId}/complete` → 204 [ADMIN]
- `GET /api/admin/tables/{tableId}/history` → List<OrderHistoryResponse> (200) [ADMIN]

### SseController
- `GET /api/sse/orders` → SseEmitter [TABLE]
- `GET /api/admin/sse/orders` → SseEmitter [ADMIN]

---

## Repository Layer

### StoreRepository
- `findByStoreCode(storeCode) -> Optional<Store>`

### StoreAdminRepository
- `findByStoreAndUsername(store, username) -> Optional<StoreAdmin>`

### TableRepository
- `findByStoreAndTableNumber(store, tableNumber) -> Optional<TableEntity>`
- `findAllByStore(store) -> List<TableEntity>`

### TableSessionRepository
- `findActiveSessionByTable(table) -> Optional<TableSession>`
- `findById(id) -> Optional<TableSession>`

### CategoryRepository
- `findAllByStoreOrderByDisplayOrder(store) -> List<Category>`

### MenuRepository
- `findByStoreAndCategoryOrderByDisplayOrder(store, category) -> List<Menu>`
- `findByIdAndStore(id, store) -> Optional<Menu>`
- `findAllByIdInAndStore(ids, store) -> List<Menu>`

### OrderRepository
- `findBySessionAndIsHistoryFalseOrderByOrderedAtAsc(session) -> List<Order>`
- `findByTableAndIsHistoryFalseOrderByOrderedAtDesc(table) -> List<Order>`
- `findBySessionAndIsHistoryTrue(session) -> List<Order>`
- `findByIdAndStore(id, store) -> Optional<Order>`
- `updateIsHistoryBySession(sessionId, isHistory) -> void` (@Modifying)
