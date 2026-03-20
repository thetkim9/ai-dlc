package com.tableorder.security;

public class AdminPrincipal {
    private final Long adminId;
    private final Long storeId;

    public AdminPrincipal(Long adminId, Long storeId) {
        this.adminId = adminId;
        this.storeId = storeId;
    }

    public Long getAdminId() { return adminId; }
    public Long getStoreId() { return storeId; }
}
