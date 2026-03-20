package com.tableorder.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.auth.dto.AdminLoginRequest;
import com.tableorder.auth.dto.AdminLoginResponse;
import com.tableorder.auth.dto.TableLoginRequest;
import com.tableorder.auth.dto.TableLoginResponse;
import com.tableorder.common.exception.ApiException;
import com.tableorder.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock AuthService authService;
    @InjectMocks AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // TC-BE-026: 관리자 로그인 성공
    @Test
    @DisplayName("TC-BE-026: POST /api/auth/admin/login - 200 OK")
    void adminLogin_success_returns200() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "username", "admin");
        setField(request, "password", "admin1234");

        given(authService.authenticateAdmin(any())).willReturn(
            new AdminLoginResponse("jwt-token", 1L, 10L));

        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.adminId").value(1))
            .andExpect(jsonPath("$.storeId").value(10));
    }

    // TC-BE-027: 잘못된 자격증명 - 401
    @Test
    @DisplayName("TC-BE-027: POST /api/auth/admin/login - 401 Unauthorized")
    void adminLogin_invalidCredentials_returns401() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "username", "admin");
        setField(request, "password", "wrong");

        given(authService.authenticateAdmin(any()))
            .willThrow(ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    // TC-BE-028: 테이블 로그인 성공
    @Test
    @DisplayName("TC-BE-028: POST /api/auth/table/login - 200 OK")
    void tableLogin_success_returns200() throws Exception {
        TableLoginRequest request = new TableLoginRequest();
        setField(request, "storeCode", "STORE001");
        setField(request, "tableNumber", 1);
        setField(request, "password", "1234");

        given(authService.authenticateTable(any())).willReturn(
            new TableLoginResponse("table-token", 2L, 20L, 1));

        mockMvc.perform(post("/api/auth/table/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("table-token"))
            .andExpect(jsonPath("$.tableNumber").value(1));
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
