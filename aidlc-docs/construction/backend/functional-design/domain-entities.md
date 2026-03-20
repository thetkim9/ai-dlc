# Domain Entities - Backend (Unit 1)

## Entity 관계도

```
Store (매장)
  |-- 1:N --> StoreAdmin (관리자 계정)
  |-- 1:N --> TableEntity (테이블)
  |-- 1:N --> Category (카테고리)
  |-- 1:N --> Menu (메뉴)

TableEntity (테이블)
  |-- 1:N --> TableSession (세션)

TableSession (세션)
  |-- 1:N --> Order (주문)

Order (주문)
  |-- 1:N --> OrderItem (주문 항목)

Menu (메뉴)
  |-- N:1 --> Category
  |-- 1:N --> OrderItem
```

---

## Entity 상세 정의

### Store
```java
@Entity
public class Store {
    Long id;
    String storeCode;       // 매장 식별자 (고유, 로그인 시 사용)
    String name;            // 매장명
    LocalDateTime createdAt;
}
```

### StoreAdmin
```java
@Entity
public class StoreAdmin {
    Long id;
    Store store;
    String username;
    String passwordHash;    // bcrypt 해시
    LocalDateTime createdAt;
    // 로그인 시도 제한용 (in-memory 또는 별도 테이블)
}
```

### TableEntity
```java
@Entity
@Table(name = "tables")
public class TableEntity {
    Long id;
    Store store;
    Integer tableNumber;    // 테이블 번호
    String passwordHash;    // 테이블 비밀번호 (bcrypt)
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### TableSession
```java
@Entity
public class TableSession {
    Long id;
    TableEntity table;
    SessionStatus status;   // ACTIVE, COMPLETED
    LocalDateTime startedAt;
    LocalDateTime completedAt;  // nullable
    LocalDateTime expiresAt;    // startedAt + 16시간
}

enum SessionStatus { ACTIVE, COMPLETED }
```

### Category
```java
@Entity
public class Category {
    Long id;
    Store store;
    String name;
    Integer displayOrder;
}
```

### Menu
```java
@Entity
public class Menu {
    Long id;
    Store store;
    Category category;
    String name;
    Integer price;
    String description;
    String imageUrl;        // 서버 내 파일 경로
    Integer displayOrder;
    Boolean available;      // 판매 가능 여부
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### Order
```java
@Entity
@Table(name = "orders")
public class Order {
    Long id;
    TableSession session;
    TableEntity table;
    Store store;
    OrderStatus status;     // PENDING, PREPARING, COMPLETED
    Integer totalAmount;
    Boolean isHistory;      // false=현재, true=과거이력
    LocalDateTime orderedAt;
    LocalDateTime completedAt;  // nullable
}

enum OrderStatus { PENDING, PREPARING, COMPLETED }
```

### OrderItem
```java
@Entity
public class OrderItem {
    Long id;
    Order order;
    Menu menu;
    String menuName;        // 주문 시점 메뉴명 스냅샷
    Integer quantity;
    Integer unitPrice;      // 주문 시점 단가 스냅샷
}
```

---

## DB 스키마 주요 제약조건

| 테이블 | 제약조건 |
|--------|---------|
| stores | storeCode UNIQUE |
| store_admins | (store_id, username) UNIQUE |
| tables | (store_id, table_number) UNIQUE |
| table_sessions | table_id에 ACTIVE 세션 최대 1개 |
| categories | (store_id, name) UNIQUE |
| menus | display_order는 카테고리 내 순서 |
| orders | session_id + is_history 조합으로 현재/과거 구분 |
