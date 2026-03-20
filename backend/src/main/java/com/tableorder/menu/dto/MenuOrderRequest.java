package com.tableorder.menu.dto;

import jakarta.validation.constraints.NotNull;

public class MenuOrderRequest {
    @NotNull private Long menuId;
    @NotNull private Integer displayOrder;

    public MenuOrderRequest() {}
    public Long getMenuId() { return menuId; }
    public Integer getDisplayOrder() { return displayOrder; }
}
