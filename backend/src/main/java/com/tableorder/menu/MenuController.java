package com.tableorder.menu;

import com.tableorder.menu.dto.*;
import com.tableorder.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(@RequestParam Long storeId) {
        return ResponseEntity.ok(menuService.getCategoriesByStore(storeId));
    }

    @GetMapping("/api/menus")
    public ResponseEntity<List<MenuResponse>> getMenus(
            @RequestParam Long storeId,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(menuService.getMenusByCategory(storeId, categoryId));
    }

    @PostMapping("/api/admin/menus")
    public ResponseEntity<MenuResponse> createMenu(
            @AuthenticationPrincipal AdminPrincipal principal,
            @Valid @RequestPart MenuCreateRequest request,
            @RequestPart(required = false) MultipartFile imageFile) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenu(principal.getStoreId(), request, imageFile));
    }

    @PutMapping("/api/admin/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long menuId,
            @Valid @RequestPart MenuUpdateRequest request,
            @RequestPart(required = false) MultipartFile imageFile) {
        return ResponseEntity.ok(menuService.updateMenu(principal.getStoreId(), menuId, request, imageFile));
    }

    @DeleteMapping("/api/admin/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long menuId) {
        menuService.deleteMenu(principal.getStoreId(), menuId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/menus/order")
    public ResponseEntity<Void> updateMenuOrder(
            @AuthenticationPrincipal AdminPrincipal principal,
            @RequestBody List<MenuOrderRequest> orderList) {
        menuService.updateMenuOrder(principal.getStoreId(), orderList);
        return ResponseEntity.noContent().build();
    }
}
