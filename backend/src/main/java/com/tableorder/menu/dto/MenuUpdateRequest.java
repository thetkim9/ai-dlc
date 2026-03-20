package com.tableorder.menu.dto;

import jakarta.validation.constraints.Min;

public class MenuUpdateRequest {
    private String name;
    @Min(1) private Integer price;
    private Long categoryId;
    private String description;
    private Boolean available;

    public MenuUpdateRequest() {}
    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public Boolean getAvailable() { return available; }
}
