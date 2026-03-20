package com.tableorder.order;

import com.tableorder.order.dto.OrderCreateRequest;
import com.tableorder.order.dto.OrderResponse;
import com.tableorder.order.dto.OrderStatusUpdateRequest;
import com.tableorder.security.AdminPrincipal;
import com.tableorder.security.TablePrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal TablePrincipal principal,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(
                        principal.getTableId(), principal.getSessionId(), principal.getStoreId(), request));
    }

    @GetMapping("/api/orders/session")
    public ResponseEntity<List<OrderResponse>> getOrdersBySession(
            @AuthenticationPrincipal TablePrincipal principal) {
        return ResponseEntity.ok(orderService.getOrdersBySession(principal.getSessionId()));
    }

    @GetMapping("/api/admin/tables/{tableId}/orders")
    public ResponseEntity<List<OrderResponse>> getActiveOrdersByTable(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getActiveOrdersByTable(principal.getStoreId(), tableId));
    }

    @PutMapping("/api/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(principal.getStoreId(), orderId, request));
    }

    @DeleteMapping("/api/admin/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long orderId) {
        orderService.deleteOrder(principal.getStoreId(), orderId);
        return ResponseEntity.noContent().build();
    }
}
