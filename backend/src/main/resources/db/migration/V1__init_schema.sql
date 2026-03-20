-- V1: 초기 스키마 생성

CREATE TABLE stores (
    id          BIGSERIAL PRIMARY KEY,
    store_code  VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE store_admins (
    id            BIGSERIAL PRIMARY KEY,
    store_id      BIGINT       NOT NULL REFERENCES stores(id),
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, username)
);

CREATE TABLE tables (
    id            BIGSERIAL PRIMARY KEY,
    store_id      BIGINT    NOT NULL REFERENCES stores(id),
    table_number  INTEGER   NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, table_number)
);

CREATE TABLE table_sessions (
    id           BIGSERIAL PRIMARY KEY,
    table_id     BIGINT      NOT NULL REFERENCES tables(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    expires_at   TIMESTAMP   NOT NULL
);

CREATE TABLE categories (
    id            BIGSERIAL PRIMARY KEY,
    store_id      BIGINT      NOT NULL REFERENCES stores(id),
    name          VARCHAR(50) NOT NULL,
    display_order INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (store_id, name)
);

CREATE TABLE menus (
    id            BIGSERIAL PRIMARY KEY,
    store_id      BIGINT       NOT NULL REFERENCES stores(id),
    category_id   BIGINT       NOT NULL REFERENCES categories(id),
    name          VARCHAR(100) NOT NULL,
    price         INTEGER      NOT NULL CHECK (price > 0),
    description   VARCHAR(500),
    image_url     VARCHAR(500),
    display_order INTEGER      NOT NULL DEFAULT 0,
    available     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    session_id   BIGINT      NOT NULL REFERENCES table_sessions(id),
    table_id     BIGINT      NOT NULL REFERENCES tables(id),
    store_id     BIGINT      NOT NULL REFERENCES stores(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount INTEGER     NOT NULL,
    is_history   BOOLEAN     NOT NULL DEFAULT FALSE,
    ordered_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_id    BIGINT       NOT NULL REFERENCES menus(id),
    menu_name  VARCHAR(100) NOT NULL,
    quantity   INTEGER      NOT NULL CHECK (quantity > 0),
    unit_price INTEGER      NOT NULL CHECK (unit_price > 0)
);

-- 인덱스
CREATE INDEX idx_table_sessions_table_status ON table_sessions(table_id, status);
CREATE INDEX idx_orders_session_history ON orders(session_id, is_history);
CREATE INDEX idx_orders_table_history ON orders(table_id, is_history);
CREATE INDEX idx_orders_store ON orders(store_id);
CREATE INDEX idx_menus_store_category ON menus(store_id, category_id);
