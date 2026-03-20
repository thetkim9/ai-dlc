package com.tableorder.order.dto;

import com.tableorder.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateRequest {
    @NotNull private OrderStatus status;

    public OrderStatusUpdateRequest() {}
    public OrderStatus getStatus() { return status; }
}
