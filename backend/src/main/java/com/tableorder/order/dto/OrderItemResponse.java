package com.tableorder.order.dto;

import com.tableorder.entity.OrderItem;

public class OrderItemResponse {
    private final Long id;
    private final Long menuId;
    private final String menuName;
    private final Integer quantity;
    private final Integer unitPrice;

    public OrderItemResponse(OrderItem item) {
        this.id = item.getId();
        this.menuId = item.getMenu().getId();
        this.menuName = item.getMenuName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
    }

    public Long getId() { return id; }
    public Long getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public Integer getQuantity() { return quantity; }
    public Integer getUnitPrice() { return unitPrice; }
}
