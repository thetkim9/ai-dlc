package com.tableorder.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.common.exception.GlobalExceptionHandler;
import com.tableorder.entity.OrderStatus;
import com.tableorder.order.dto.OrderCreateRequest;
import com.tableorder.order.dto.OrderItemRequest;
import com.tableorder.order.dto.OrderStatusUpdateRequest;
import com.tableorder.security.AdminPrincipal;
import com.tableorder.security.TablePrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock OrderService orderService;
    @InjectMocks OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.clearContext();
    }

    // TC-BE-029: 주문 생성 - 201
    @Test
    @DisplayName("TC-BE-029: POST /api/orders - 201 Created")
    void createOrder_success_returns201() throws Exception {
        TablePrincipal principal = new TablePrincipal(2L, 20L, 1L);
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
            List.of(new SimpleGrantedAuthority("ROLE_TABLE")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        OrderItemRequest item = new OrderItemRequest();
        setField(item, "menuId", 100L);
        setField(item, "quantity", 2);

        OrderCreateRequest request = new OrderCreateRequest();
        setField(request, "items", List.of(item));

        given(orderService.createOrder(eq(2L), eq(20L), eq(1L), any())).willReturn(null);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    // TC-BE-030: 관리자 주문 상태 변경 - 200
    @Test
    @DisplayName("TC-BE-030: PUT /api/admin/orders/{orderId}/status - 200 OK")
    void updateOrderStatus_admin_returns200() throws Exception {
        AdminPrincipal principal = new AdminPrincipal(1L, 1L);
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        setField(request, "status", OrderStatus.PREPARING);

        given(orderService.updateOrderStatus(eq(1L), eq(1000L), any())).willReturn(null);

        mockMvc.perform(put("/api/admin/orders/1000/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
