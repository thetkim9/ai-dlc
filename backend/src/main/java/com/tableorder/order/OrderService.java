package com.tableorder.order;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.order.dto.OrderCreateRequest;
import com.tableorder.order.dto.OrderItemRequest;
import com.tableorder.order.dto.OrderResponse;
import com.tableorder.order.dto.OrderStatusUpdateRequest;
import com.tableorder.repository.*;
import com.tableorder.sse.SseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final TableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final StoreRepository storeRepository;
    private final SseService sseService;

    public OrderService(OrderRepository orderRepository,
                        MenuRepository menuRepository,
                        TableRepository tableRepository,
                        TableSessionRepository tableSessionRepository,
                        StoreRepository storeRepository,
                        SseService sseService) {
        this.orderRepository = orderRepository;
        this.menuRepository = menuRepository;
        this.tableRepository = tableRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.storeRepository = storeRepository;
        this.sseService = sseService;
    }

    @Transactional
    public OrderResponse createOrder(Long tableId, Long sessionId, Long storeId, OrderCreateRequest request) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> ApiException.notFound("테이블을 찾을 수 없습니다."));
        TableSession session = tableSessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("세션을 찾을 수 없습니다."));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));

        // 세션 만료 검증
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ApiException.forbidden("세션이 만료되었습니다.");
        }

        // 빈 주문 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw ApiException.badRequest("주문 항목이 비어있습니다.");
        }

        // 메뉴 조회 및 검증
        List<Long> menuIds = request.getItems().stream()
                .map(OrderItemRequest::getMenuId).collect(Collectors.toList());
        List<Menu> menus = menuRepository.findAllByIdInAndStore(menuIds, store);

        if (menus.size() != menuIds.stream().distinct().count()) {
            throw ApiException.notFound("존재하지 않는 메뉴가 포함되어 있습니다.");
        }

        Map<Long, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // 주문 생성
        Order order = new Order();
        order.setSession(session);
        order.setTable(table);
        order.setStore(store);
        order.setStatus(OrderStatus.PENDING);
        order.setIsHistory(false);
        order.setOrderedAt(LocalDateTime.now());

        int totalAmount = 0;
        for (OrderItemRequest itemReq : request.getItems()) {
            Menu menu = menuMap.get(itemReq.getMenuId());
            if (!menu.getAvailable()) {
                throw ApiException.badRequest("판매 중지된 메뉴입니다: " + menu.getName());
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenu(menu);
            item.setMenuName(menu.getName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(menu.getPrice());
            order.getItems().add(item);
            totalAmount += menu.getPrice() * itemReq.getQuantity();
        }
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);

        // SSE 이벤트 발행
        sseService.sendToAdmin(storeId, "new-order", new OrderResponse(saved));

        return new OrderResponse(saved);
    }

    public List<OrderResponse> getOrdersBySession(Long sessionId) {
        TableSession session = tableSessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("세션을 찾을 수 없습니다."));
        return orderRepository.findBySessionAndIsHistoryFalseOrderByOrderedAtAsc(session)
                .stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    public List<OrderResponse> getActiveOrdersByTable(Long storeId, Long tableId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> ApiException.notFound("테이블을 찾을 수 없습니다."));
        return orderRepository.findByTableAndIsHistoryFalseOrderByOrderedAtDesc(table)
                .stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long storeId, Long orderId, OrderStatusUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
        Order order = orderRepository.findByIdAndStore(orderId, store)
                .orElseThrow(() -> ApiException.notFound("주문을 찾을 수 없습니다."));

        validateStatusTransition(order.getStatus(), request.getStatus());
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);

        // SSE 이벤트 발행
        sseService.sendToAdmin(storeId, "order-status-changed", new OrderResponse(saved));
        sseService.sendToTable(order.getSession().getId(), "order-status-changed", new OrderResponse(saved));

        return new OrderResponse(saved);
    }

    @Transactional
    public void deleteOrder(Long storeId, Long orderId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
        Order order = orderRepository.findByIdAndStore(orderId, store)
                .orElseThrow(() -> ApiException.notFound("주문을 찾을 수 없습니다."));
        orderRepository.delete(order);
        sseService.sendToAdmin(storeId, "order-deleted", Map.of("orderId", orderId));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PREPARING;
            case PREPARING -> next == OrderStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) {
            throw ApiException.badRequest(
                "잘못된 상태 전이입니다: " + current + " -> " + next);
        }
    }
}
