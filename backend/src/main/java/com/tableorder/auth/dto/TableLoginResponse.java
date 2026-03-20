package com.tableorder.auth.dto;

public class TableLoginResponse {
    private final String token;
    private final Long tableId;
    private final Long sessionId;
    private final Integer tableNumber;

    public TableLoginResponse(String token, Long tableId, Long sessionId, Integer tableNumber) {
        this.token = token;
        this.tableId = tableId;
        this.sessionId = sessionId;
        this.tableNumber = tableNumber;
    }

    public String getToken() { return token; }
    public Long getTableId() { return tableId; }
    public Long getSessionId() { return sessionId; }
    public Integer getTableNumber() { return tableNumber; }
}
