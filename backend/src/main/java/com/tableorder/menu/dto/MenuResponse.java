package com.tableorder.menu.dto;

import com.tableorder.entity.Menu;

public class MenuResponse {
    private final Long id;
    private final Long categoryId;
    private final String name;
    private final Integer price;
    private final String description;
    private final String imageUrl;
    private final Integer displayOrder;
    private final Boolean available;

    public MenuResponse(Menu menu) {
        this.id = menu.getId();
        this.categoryId = menu.getCategory().getId();
        this.name = menu.getName();
        this.price = menu.getPrice();
        this.description = menu.getDescription();
        this.imageUrl = menu.getImageUrl();
        this.displayOrder = menu.getDisplayOrder();
        this.available = menu.getAvailable();
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Boolean getAvailable() { return available; }
}
