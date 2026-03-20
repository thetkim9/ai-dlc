package com.tableorder.table;

import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.repository.*;
import com.tableorder.sse.SseService;
import com.tableorder.table.dto.TableSetupRequest;
import com.tableorder.table.dto.TableSetupResponse;
import com.tableorder.table.dto.TableSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock TableRepository tableRepository;
    @Mock TableSessionRepository tableSessionRepository;
    @Mock OrderRepository orderRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SseService sseService;

    @InjectMocks TableService tableService;

    private Store store;
    private TableEntity table;
    private TableSession activeSession;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setStoreCode("STORE001");

        table = new TableEntity();
        table.setId(2L);
        table.setStore(store);
        table.setTableNumber(1);
        table.setPasswordHash("$2a$hashed");

        activeSession = new TableSession();
        activeSession.setId(20L);
        activeSession.setTable(table);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartedAt(LocalDateTime.now());
        activeSession.setExpiresAt(LocalDateTime.now().plusHours(16));
    }

    // TC-BE-019: 신규 세션 생성
    @Test
    @DisplayName("TC-BE-019: 기존 세션 없을 때 신규 세션 생성")
    void setupTable_noExistingSession_createsNewSession() {
        // given
        TableSetupRequest request = new TableSetupRequest();
        setField(request, "password", "newpass");

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.empty());
        given(passwordEncoder.encode("newpass")).willReturn("$2a$newHash");
        given(tableSessionRepository.save(any(TableSession.class))).willReturn(activeSession);

        // when
        TableSetupResponse response = tableService.setupTable(1L, 2L, request);

        // then
        assertThat(response).isNotNull();
        then(tableSessionRepository).should().save(any(TableSession.class));
    }

    // TC-BE-020: 기존 세션 종료 후 재생성
    @Test
    @DisplayName("TC-BE-020: 기존 ACTIVE 세션 종료 후 새 세션 생성")
    void setupTable_existingSession_completesAndCreatesNew() {
        // given
        TableSetupRequest request = new TableSetupRequest();
        setField(request, "password", "newpass");

        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.of(activeSession));
        given(passwordEncoder.encode("newpass")).willReturn("$2a$newHash");
        given(tableSessionRepository.save(any(TableSession.class))).willReturn(activeSession);

        // when
        tableService.setupTable(1L, 2L, request);

        // then - 기존 세션 COMPLETED 처리 + 새 세션 저장 (2번 save 호출)
        then(tableSessionRepository).should(times(2)).save(any(TableSession.class));
    }

    // TC-BE-021: 이용 완료 성공
    @Test
    @DisplayName("TC-BE-021: 이용 완료 처리 성공")
    void completeSession_activeSession_success() {
        // given
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.of(activeSession));

        // when
        tableService.completeSession(1L, 2L);

        // then
        assertThat(activeSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(activeSession.getCompletedAt()).isNotNull();
        then(orderRepository).should().updateIsHistoryBySessionId(20L, true);
        then(sseService).should().sendToAdmin(eq(1L), eq("table-reset"), any());
    }

    // TC-BE-022: 세션 없음
    @Test
    @DisplayName("TC-BE-022: ACTIVE 세션 없을 때 이용 완료 - 404")
    void completeSession_noActiveSession_throws404() {
        // given
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(tableRepository.findById(2L)).willReturn(Optional.of(table));
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tableService.completeSession(1L, 2L))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("테이블 목록 조회 성공")
    void getTablesByStore_success() {
        given(storeRepository.findById(1L)).willReturn(Optional.of(store));
        given(tableRepository.findAllByStore(store)).willReturn(List.of(table));
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.of(activeSession));

        List<TableSummaryResponse> result = tableService.getTablesByStore(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTableNumber()).isEqualTo(1);
        assertThat(result.get(0).getSessionStatus()).isEqualTo(SessionStatus.ACTIVE);
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
