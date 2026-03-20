package com.tableorder.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {
    @NotNull private Long menuId;
    @NotNull @Min(1) private Integer quantity;

    public OrderItemRequest() {}
    public Long getMenuId() { return menuId; }
    public Integer getQuantity() { return quantity; }
}
