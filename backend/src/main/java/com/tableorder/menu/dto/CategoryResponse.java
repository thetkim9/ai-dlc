package com.tableorder.menu.dto;

import com.tableorder.entity.Category;

public class CategoryResponse {
    private final Long id;
    private final String name;
    private final Integer displayOrder;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.displayOrder = category.getDisplayOrder();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getDisplayOrder() { return displayOrder; }
}
