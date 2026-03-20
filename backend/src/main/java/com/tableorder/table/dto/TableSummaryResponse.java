package com.tableorder.table.dto;

import com.tableorder.entity.SessionStatus;
import com.tableorder.entity.TableEntity;
import com.tableorder.entity.TableSession;

public class TableSummaryResponse {
    private final Long id;
    private final Integer tableNumber;
    private final SessionStatus sessionStatus;
    private final Long sessionId;

    public TableSummaryResponse(TableEntity table, TableSession activeSession) {
        this.id = table.getId();
        this.tableNumber = table.getTableNumber();
        this.sessionStatus = activeSession != null ? activeSession.getStatus() : null;
        this.sessionId = activeSession != null ? activeSession.getId() : null;
    }

    public Long getId() { return id; }
    public Integer getTableNumber() { return tableNumber; }
    public SessionStatus getSessionStatus() { return sessionStatus; }
    public Long getSessionId() { return sessionId; }
}
