package com.tableorder.table.dto;

import jakarta.validation.constraints.NotBlank;

public class TableSetupRequest {
    @NotBlank private String password;

    public TableSetupRequest() {}
    public String getPassword() { return password; }
}
