package com.tableorder.table.dto;

import java.time.LocalDateTime;

public class TableSetupResponse {
    private final Long tableId;
    private final Long sessionId;
    private final Integer tableNumber;
    private final LocalDateTime expiresAt;

    public TableSetupResponse(Long tableId, Long sessionId, Integer tableNumber, LocalDateTime expiresAt) {
        this.tableId = tableId;
        this.sessionId = sessionId;
        this.tableNumber = tableNumber;
        this.expiresAt = expiresAt;
    }

    public Long getTableId() { return tableId; }
    public Long getSessionId() { return sessionId; }
    public Integer getTableNumber() { return tableNumber; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
