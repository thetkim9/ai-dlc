package com.tableorder.auth.dto;

public class AdminLoginResponse {
    private final String token;
    private final Long adminId;
    private final Long storeId;

    public AdminLoginResponse(String token, Long adminId, Long storeId) {
        this.token = token;
        this.adminId = adminId;
        this.storeId = storeId;
    }

    public String getToken() { return token; }
    public Long getAdminId() { return adminId; }
    public Long getStoreId() { return storeId; }
}
