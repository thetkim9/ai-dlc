package com.tableorder.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    private final Map<Long, List<SseEmitter>> adminEmitters = new ConcurrentHashMap<>();
    private final Map<Long, List<SseEmitter>> tableEmitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter createAdminEmitter(Long storeId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        adminEmitters.computeIfAbsent(storeId, k -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeAdminEmitter(storeId, emitter));
        emitter.onTimeout(() -> removeAdminEmitter(storeId, emitter));
        emitter.onError(e -> removeAdminEmitter(storeId, emitter));

        // 연결 확인용 초기 이벤트
        sendEvent(emitter, "connected", Map.of("storeId", storeId));
        return emitter;
    }

    public SseEmitter createTableEmitter(Long sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        tableEmitters.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeTableEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeTableEmitter(sessionId, emitter));
        emitter.onError(e -> removeTableEmitter(sessionId, emitter));

        sendEvent(emitter, "connected", Map.of("sessionId", sessionId));
        return emitter;
    }

    public void sendToAdmin(Long storeId, String eventName, Object data) {
        List<SseEmitter> emitters = adminEmitters.getOrDefault(storeId, List.of());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                sendEvent(emitter, eventName, data);
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    public void sendToTable(Long sessionId, String eventName, Object data) {
        List<SseEmitter> emitters = tableEmitters.getOrDefault(sessionId, List.of());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                sendEvent(emitter, eventName, data);
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event().name(eventName).data(json));
        } catch (IOException e) {
            emitter.completeWithError(e);
            throw new RuntimeException("SSE 이벤트 전송 실패", e);
        }
    }

    private void removeAdminEmitter(Long storeId, SseEmitter emitter) {
        List<SseEmitter> emitters = adminEmitters.get(storeId);
        if (emitters != null) emitters.remove(emitter);
    }

    private void removeTableEmitter(Long sessionId, SseEmitter emitter) {
        List<SseEmitter> emitters = tableEmitters.get(sessionId);
        if (emitters != null) emitters.remove(emitter);
    }
}
