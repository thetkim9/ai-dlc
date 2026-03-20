package com.tableorder.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {
    @NotBlank private String storeCode;
    @NotBlank private String username;
    @NotBlank private String password;

    public AdminLoginRequest() {}
    public String getStoreCode() { return storeCode; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
