package com.tableorder.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
            16L
        );
    }

    @Test
    @DisplayName("관리자 토큰 생성 후 파싱 성공")
    void generateAdminToken_thenParse_success() {
        // given
        Long adminId = 1L;
        Long storeId = 10L;

        // when
        String token = jwtUtil.generateAdminToken(adminId, storeId);
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims.get("adminId", Long.class)).isEqualTo(adminId);
        assertThat(claims.get("storeId", Long.class)).isEqualTo(storeId);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("테이블 토큰 생성 후 파싱 성공")
    void generateTableToken_thenParse_success() {
        // given
        Long tableId = 2L;
        Long sessionId = 20L;
        Long storeId = 10L;

        // when
        String token = jwtUtil.generateTableToken(tableId, sessionId, storeId);
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims.get("tableId", Long.class)).isEqualTo(tableId);
        assertThat(claims.get("sessionId", Long.class)).isEqualTo(sessionId);
        assertThat(claims.get("storeId", Long.class)).isEqualTo(storeId);
        assertThat(claims.get("role", String.class)).isEqualTo("TABLE");
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 예외 발생")
    void parseExpiredToken_throwsException() {
        // given - 만료 시간 0시간인 JwtUtil
        JwtUtil expiredJwtUtil = new JwtUtil(
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
            0L
        );
        String token = expiredJwtUtil.generateAdminToken(1L, 1L);

        // when & then
        assertThatThrownBy(() -> jwtUtil.parseToken(token))
            .isInstanceOf(com.tableorder.common.exception.ApiException.class);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 파싱 시 예외 발생")
    void parseInvalidToken_throwsException() {
        assertThatThrownBy(() -> jwtUtil.parseToken("invalid.token.here"))
            .isInstanceOf(com.tableorder.common.exception.ApiException.class);
    }
}
