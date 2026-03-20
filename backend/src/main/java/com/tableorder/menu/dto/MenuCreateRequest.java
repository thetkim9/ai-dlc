package com.tableorder.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MenuCreateRequest {
    @NotBlank
    private String name;
    @NotNull @Min(1)
    private Integer price;
    @NotNull
    private Long categoryId;
    private String description;
    private Boolean available = true;

    public MenuCreateRequest() {}

    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public Boolean getAvailable() { return available; }
}
