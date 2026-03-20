package com.tableorder.order;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.order.dto.OrderCreateRequest;
import com.tableorder.order.dto.OrderItemRequest;
import com.tableorder.order.dto.OrderResponse;
import com.tableorder.order.dto.OrderStatusUpdateRequest;
import com.tableorder.repository.*;
import com.tableorder.sse.SseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock MenuRepository menuRepository;
    @Mock TableRepository tableRepository;
    @Mock TableSessionRepository tableSessionRepository;
    @Mock StoreRepository storeRepository;
    @Mock SseService sseService;

    @InjectMocks OrderService orderService;

    private Store store;
    private TableEntity table;
    private TableSession activeSession;
    private Menu menu;
    private Order order;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);

        table = new TableEntity();
        table.setId(2L);
        table.setStore(store);
        table.setTableNumber(1);

        activeSession = new TableSession();
        activeSession.setId(20L);
        activeSession.setTable(table);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartedAt(LocalDateTime.now());
        activeSession.setExpiresAt(LocalDateTime.now().plusHours(16));

        menu = new Menu();
        menu.setId(100L);
        menu.setStore(store);
        menu.setName("불고기 버거");
        menu.setPrice(8900);
        menu.setAvailable(true);

        order = new Order();
        order.setId(1000L);
        order.setSession(activeSession);
        order.setTable(table);
        order.setStore(store);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(8900);
        order.setIsHistory(false);
        order.setOrderedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
    }

    // TC-BE-012: 주문 생성 성공 + SSE 발행 검증
    @Test
    @DisplayName("TC-BE-012: 주문 생성 성공 및 SSE 이벤트 발행")
    void createOrder_valid_successAndSsePublished() {
        // given
        OrderCreateRequest request = buildOrderRequest(100L, 1);

        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findById(20L)).willReturn(Optional.of(activeSession));
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(menuRepository.findAllByIdInAndStore(List.of(100L), store)).willReturn(List.of(menu));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        OrderResponse response = orderService.createOrder(2L, 20L, 1L, request);

        // then
        assertThat(response).isNotNull();
        then(sseService).should().sendToAdmin(eq(1L), eq("new-order"), any());
    }

    // TC-BE-013: 빈 주문
    @Test
    @DisplayName("TC-BE-013: 빈 주문 항목 - 400")
    void createOrder_emptyItems_throws400() {
        OrderCreateRequest request = new OrderCreateRequest();
        setField(request, "items", List.of());

        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findById(20L)).willReturn(Optional.of(activeSession));
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));

        assertThatThrownBy(() -> orderService.createOrder(2L, 20L, 1L, request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // TC-BE-014: 존재하지 않는 메뉴
    @Test
    @DisplayName("TC-BE-014: 존재하지 않는 메뉴 주문 - 404")
    void createOrder_menuNotFound_throws404() {
        OrderCreateRequest request = buildOrderRequest(999L, 1);

        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findById(20L)).willReturn(Optional.of(activeSession));
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(menuRepository.findAllByIdInAndStore(List.of(999L), store)).willReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(2L, 20L, 1L, request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // TC-BE-015: 만료된 세션
    @Test
    @DisplayName("TC-BE-015: 만료된 세션으로 주문 - 403")
    void createOrder_expiredSession_throws403() {
        activeSession.setExpiresAt(LocalDateTime.now().minusHours(1)); // 만료
        OrderCreateRequest request = buildOrderRequest(100L, 1);

        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findById(20L)).willReturn(Optional.of(activeSession));
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));

        assertThatThrownBy(() -> orderService.createOrder(2L, 20L, 1L, request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    // TC-BE-016: 주문 상태 변경 성공 + SSE 발행
    @Test
    @DisplayName("TC-BE-016: PENDING -> PREPARING 상태 변경 성공 및 SSE 발행")
    void updateOrderStatus_pendingToPreparing_successAndSse() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        setField(request, "status", OrderStatus.PREPARING);

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(orderRepository.findByIdAndStore(1000L, store)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        OrderResponse response = orderService.updateOrderStatus(1L, 1000L, request);

        assertThat(response).isNotNull();
        then(sseService).should().sendToAdmin(eq(1L), eq("order-status-changed"), any());
    }

    // TC-BE-017: 역방향 상태 전이 실패
    @Test
    @DisplayName("TC-BE-017: COMPLETED -> PENDING 역방향 전이 - 400")
    void updateOrderStatus_reverseTransition_throws400() {
        order.setStatus(OrderStatus.COMPLETED);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        setField(request, "status", OrderStatus.PENDING);

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(orderRepository.findByIdAndStore(1000L, store)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, 1000L, request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // TC-BE-018: 현재 세션 주문만 반환
    @Test
    @DisplayName("TC-BE-018: 현재 세션의 isHistory=false 주문만 반환")
    void getOrdersBySession_returnsCurrentSessionOrders() {
        given(tableSessionRepository.findById(20L)).willReturn(Optional.of(activeSession));
        given(orderRepository.findBySessionAndIsHistoryFalseOrderByOrderedAtAsc(activeSession))
            .willReturn(List.of(order));

        List<OrderResponse> result = orderService.getOrdersBySession(20L);

        assertThat(result).hasSize(1);
    }

    private OrderCreateRequest buildOrderRequest(Long menuId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        setField(item, "menuId", menuId);
        setField(item, "quantity", quantity);

        OrderCreateRequest request = new OrderCreateRequest();
        setField(request, "items", List.of(item));
        return request;
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
