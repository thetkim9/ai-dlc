package com.tableorder.security;

public class TablePrincipal {
    private final Long tableId;
    private final Long sessionId;
    private final Long storeId;

    public TablePrincipal(Long tableId, Long sessionId, Long storeId) {
        this.tableId = tableId;
        this.sessionId = sessionId;
        this.storeId = storeId;
    }

    public Long getTableId() { return tableId; }
    public Long getSessionId() { return sessionId; }
    public Long getStoreId() { return storeId; }
}
