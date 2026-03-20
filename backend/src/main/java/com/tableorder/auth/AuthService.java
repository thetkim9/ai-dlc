package com.tableorder.auth;

import com.tableorder.auth.dto.AdminLoginRequest;
import com.tableorder.auth.dto.AdminLoginResponse;
import com.tableorder.auth.dto.TableLoginRequest;
import com.tableorder.auth.dto.TableLoginResponse;
import com.tableorder.common.exception.ApiException;
import com.tableorder.entity.*;
import com.tableorder.repository.*;
import com.tableorder.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;

    private final StoreRepository storeRepository;
    private final StoreAdminRepository storeAdminRepository;
    private final TableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(StoreRepository storeRepository,
                       StoreAdminRepository storeAdminRepository,
                       TableRepository tableRepository,
                       TableSessionRepository tableSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.storeRepository = storeRepository;
        this.storeAdminRepository = storeAdminRepository;
        this.tableRepository = tableRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // in-memory 로그인 시도 카운터: key = "{storeCode}:{username}"
    private final Map<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();

    public AdminLoginResponse authenticateAdmin(AdminLoginRequest request) {
        String key = request.getStoreCode() + ":" + request.getUsername();

        // 잠금 확인
        checkLoginAttempts(key);

        // 매장 조회
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> {
                    recordLoginFailure(key);
                    return ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
                });

        // 관리자 조회
        StoreAdmin admin = storeAdminRepository.findByStoreAndUsername(store, request.getUsername())
                .orElseThrow(() -> {
                    recordLoginFailure(key);
                    return ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
                });

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            recordLoginFailure(key);
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        clearLoginAttempts(key);
        String token = jwtUtil.generateAdminToken(admin.getId(), store.getId());
        return new AdminLoginResponse(token, admin.getId(), store.getId());
    }

    public TableLoginResponse authenticateTable(TableLoginRequest request) {
        // 매장 조회
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> ApiException.unauthorized("매장 정보를 찾을 수 없습니다."));

        // 테이블 조회
        TableEntity table = tableRepository.findByStoreAndTableNumber(store, request.getTableNumber())
                .orElseThrow(() -> ApiException.unauthorized("테이블 정보를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), table.getPasswordHash())) {
            throw ApiException.unauthorized("비밀번호가 올바르지 않습니다.");
        }

        // ACTIVE 세션 확인
        TableSession session = tableSessionRepository.findByTableAndStatus(table, SessionStatus.ACTIVE)
                .orElseThrow(() -> ApiException.unauthorized("활성 세션이 없습니다. 관리자에게 테이블 초기 설정을 요청하세요."));

        String token = jwtUtil.generateTableToken(table.getId(), session.getId(), store.getId());
        return new TableLoginResponse(token, table.getId(), session.getId(), table.getTableNumber());
    }

    public void checkLoginAttempts(String key) {
        AtomicInteger count = loginAttempts.get(key);
        if (count != null && count.get() >= MAX_ATTEMPTS) {
            throw ApiException.tooManyRequests("로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도하세요.");
        }
    }

    public void recordLoginFailure(String key) {
        loginAttempts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void clearLoginAttempts(String key) {
        loginAttempts.remove(key);
    }
}
