package com.tableorder.table.dto;

import com.tableorder.entity.Order;
import com.tableorder.entity.OrderStatus;
import com.tableorder.order.dto.OrderItemResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderHistoryResponse {
    private final Long id;
    private final Integer tableNumber;
    private final OrderStatus status;
    private final Integer totalAmount;
    private final LocalDateTime orderedAt;
    private final List<OrderItemResponse> items;

    public OrderHistoryResponse(Order order) {
        this.id = order.getId();
        this.tableNumber = order.getTable().getTableNumber();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.orderedAt = order.getOrderedAt();
        this.items = order.getItems().stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
    }

    public Long getId() { return id; }
    public Integer getTableNumber() { return tableNumber; }
    public OrderStatus getStatus() { return status; }
    public Integer getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public List<OrderItemResponse> getItems() { return items; }
}
