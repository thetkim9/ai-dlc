package com.tableorder.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TableLoginRequest {
    @NotBlank private String storeCode;
    @NotNull  private Integer tableNumber;
    @NotBlank private String password;

    public TableLoginRequest() {}
    public String getStoreCode() { return storeCode; }
    public Integer getTableNumber() { return tableNumber; }
    public String getPassword() { return password; }
}
