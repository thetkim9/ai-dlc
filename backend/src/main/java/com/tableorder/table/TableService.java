package com.tableorder.table;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.repository.*;
import com.tableorder.sse.SseService;
import com.tableorder.table.dto.OrderHistoryResponse;
import com.tableorder.table.dto.TableSetupRequest;
import com.tableorder.table.dto.TableSetupResponse;
import com.tableorder.table.dto.TableSummaryResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TableService {

    private final StoreRepository storeRepository;
    private final TableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final SseService sseService;

    public TableService(StoreRepository storeRepository,
                        TableRepository tableRepository,
                        TableSessionRepository tableSessionRepository,
                        OrderRepository orderRepository,
                        PasswordEncoder passwordEncoder,
                        SseService sseService) {
        this.storeRepository = storeRepository;
        this.tableRepository = tableRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.sseService = sseService;
    }

    public List<TableSummaryResponse> getTablesByStore(Long storeId) {
        Store store = getStore(storeId);
        List<TableEntity> tables = tableRepository.findAllByStore(store);
        return tables.stream().map(table -> {
            Optional<TableSession> session =
                tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE);
            return new TableSummaryResponse(table, session.orElse(null));
        }).collect(Collectors.toList());
    }

    @Transactional
    public TableSetupResponse setupTable(Long storeId, Long tableId, TableSetupRequest request) {
        Store store = getStore(storeId);
        TableEntity table = getTable(tableId);

        // 기존 ACTIVE 세션 종료
        tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
            .ifPresent(existing -> {
                existing.setStatus(SessionStatus.COMPLETED);
                existing.setCompletedAt(LocalDateTime.now());
                tableSessionRepository.save(existing);
            });

        // 테이블 비밀번호 업데이트
        table.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        table.setUpdatedAt(LocalDateTime.now());

        // 새 세션 생성
        TableSession newSession = new TableSession();
        newSession.setTable(table);
        newSession.setStatus(SessionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        newSession.setStartedAt(now);
        newSession.setExpiresAt(now.plusHours(16));
        TableSession saved = tableSessionRepository.save(newSession);

        return new TableSetupResponse(table.getId(), saved.getId(), table.getTableNumber(), saved.getExpiresAt());
    }

    @Transactional
    public void completeSession(Long storeId, Long tableId) {
        Store store = getStore(storeId);
        TableEntity table = getTable(tableId);

        TableSession session = tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElseThrow(() -> ApiException.notFound("활성 세션이 없습니다."));

        // 주문 이력 처리
        orderRepository.updateIsHistoryBySessionId(session.getId(), true);

        // 세션 종료
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        tableSessionRepository.save(session);

        // SSE 이벤트 발행
        sseService.sendToAdmin(storeId, "table-reset",
            Map.of("tableId", tableId, "tableNumber", table.getTableNumber()));
    }

    public List<OrderHistoryResponse> getOrderHistory(Long storeId, Long tableId, LocalDate from, LocalDate to) {
        Store store = getStore(storeId);
        TableEntity table = getTable(tableId);

        // isHistory=true 주문 조회 (날짜 필터는 애플리케이션 레벨에서 처리)
        return orderRepository.findByTableAndIsHistoryFalseOrderByOrderedAtDesc(table)
                .stream()
                .filter(o -> {
                    if (from != null && o.getOrderedAt().toLocalDate().isBefore(from)) return false;
                    if (to != null && o.getOrderedAt().toLocalDate().isAfter(to)) return false;
                    return true;
                })
                .map(OrderHistoryResponse::new)
                .collect(Collectors.toList());
    }

    private Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
    }

    private TableEntity getTable(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> ApiException.notFound("테이블을 찾을 수 없습니다."));
    }
}
