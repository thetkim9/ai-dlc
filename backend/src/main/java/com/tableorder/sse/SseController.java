package com.tableorder.sse;

import com.tableorder.security.AdminPrincipal;
import com.tableorder.security.TablePrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/api/sse/orders")
    public SseEmitter tableSubscribe(@AuthenticationPrincipal TablePrincipal principal) {
        return sseService.createTableEmitter(principal.getSessionId());
    }

    @GetMapping("/api/admin/sse/orders")
    public SseEmitter adminSubscribe(@AuthenticationPrincipal AdminPrincipal principal) {
        return sseService.createAdminEmitter(principal.getStoreId());
    }
}
