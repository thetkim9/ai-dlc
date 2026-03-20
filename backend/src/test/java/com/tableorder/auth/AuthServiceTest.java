package com.tableorder.auth;

import com.tableorder.auth.dto.AdminLoginRequest;
import com.tableorder.auth.dto.AdminLoginResponse;
import com.tableorder.auth.dto.TableLoginRequest;
import com.tableorder.auth.dto.TableLoginResponse;
import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.repository.*;
import com.tableorder.security.JwtUtil;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock StoreAdminRepository storeAdminRepository;
    @Mock TableRepository tableRepository;
    @Mock TableSessionRepository tableSessionRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks AuthService authService;

    private Store store;
    private StoreAdmin admin;
    private TableEntity table;
    private TableSession activeSession;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setStoreCode("STORE001");
        store.setName("테스트 매장");

        admin = new StoreAdmin();
        admin.setId(10L);
        admin.setStore(store);
        admin.setUsername("admin");
        admin.setPasswordHash("$2a$hashed");

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

    // TC-BE-001: 유효한 자격증명 관리자 로그인 성공
    @Test
    @DisplayName("TC-BE-001: 유효한 자격증명으로 관리자 로그인 성공")
    void authenticateAdmin_validCredentials_success() {
        // given
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "username", "admin");
        setField(request, "password", "admin1234");

        given(storeRepository.findByStoreCode("STORE001")).willReturn(Optional.of(store));
        given(storeAdminRepository.findByStoreAndUsername(store, "admin")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("admin1234", "$2a$hashed")).willReturn(true);
        given(jwtUtil.generateAdminToken(10L, 1L)).willReturn("admin-jwt-token");

        // when
        AdminLoginResponse response = authService.authenticateAdmin(request);

        // then
        assertThat(response.getToken()).isEqualTo("admin-jwt-token");
        assertThat(response.getAdminId()).isEqualTo(10L);
        assertThat(response.getStoreId()).isEqualTo(1L);
    }

    // TC-BE-002: 잘못된 비밀번호
    @Test
    @DisplayName("TC-BE-002: 잘못된 비밀번호로 로그인 실패 - 401")
    void authenticateAdmin_wrongPassword_throws401() {
        // given
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "username", "admin");
        setField(request, "password", "wrongpass");

        given(storeRepository.findByStoreCode("STORE001")).willReturn(Optional.of(store));
        given(storeAdminRepository.findByStoreAndUsername(store, "admin")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches("wrongpass", "$2a$hashed")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.authenticateAdmin(request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // TC-BE-003: 존재하지 않는 매장
    @Test
    @DisplayName("TC-BE-003: 존재하지 않는 매장 코드로 로그인 실패 - 401")
    void authenticateAdmin_storeNotFound_throws401() {
        // given
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "INVALID");
        setField(request, "username", "admin");
        setField(request, "password", "admin1234");

        given(storeRepository.findByStoreCode("INVALID")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.authenticateAdmin(request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // TC-BE-004: 5회 실패 후 잠금
    @Test
    @DisplayName("TC-BE-004: 5회 연속 실패 후 잠금 - 429")
    void authenticateAdmin_afterFiveFailures_throws429() {
        // given
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "username", "admin");
        setField(request, "password", "wrongpass");

        given(storeRepository.findByStoreCode("STORE001")).willReturn(Optional.of(store));
        given(storeAdminRepository.findByStoreAndUsername(store, "admin")).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // 5회 실패
        for (int i = 0; i < 5; i++) {
            try { authService.authenticateAdmin(request); } catch (ApiException ignored) {}
        }

        // when & then - 6번째 시도는 429
        assertThatThrownBy(() -> authService.authenticateAdmin(request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    // TC-BE-005: 테이블 로그인 성공
    @Test
    @DisplayName("TC-BE-005: 유효한 자격증명으로 테이블 로그인 성공")
    void authenticateTable_validCredentials_success() {
        // given
        TableLoginRequest request = new TableLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "tableNumber", 1);
        setField(request, "password", "1234");

        given(storeRepository.findByStoreCode("STORE001")).willReturn(Optional.of(store));
        given(tableRepository.findByStoreAndTableNumber(store, 1)).willReturn(Optional.of(table));
        given(passwordEncoder.matches("1234", "$2a$hashed")).willReturn(true);
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.of(activeSession));
        given(jwtUtil.generateTableToken(2L, 20L, 1L)).willReturn("table-jwt-token");

        // when
        TableLoginResponse response = authService.authenticateTable(request);

        // then
        assertThat(response.getToken()).isEqualTo("table-jwt-token");
        assertThat(response.getTableId()).isEqualTo(2L);
        assertThat(response.getSessionId()).isEqualTo(20L);
        assertThat(response.getTableNumber()).isEqualTo(1);
    }

    // TC-BE-006: ACTIVE 세션 없음
    @Test
    @DisplayName("TC-BE-006: ACTIVE 세션 없는 테이블 로그인 실패 - 401")
    void authenticateTable_noActiveSession_throws401() {
        // given
        TableLoginRequest request = new TableLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "tableNumber", 1);
        setField(request, "password", "1234");

        given(storeRepository.findByStoreCode("STORE001")).willReturn(Optional.of(store));
        given(tableRepository.findByStoreAndTableNumber(store, 1)).willReturn(Optional.of(table));
        given(passwordEncoder.matches("1234", "$2a$hashed")).willReturn(true);
        given(tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.authenticateTable(request))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // 리플렉션으로 private 필드 설정 (테스트 편의)
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
