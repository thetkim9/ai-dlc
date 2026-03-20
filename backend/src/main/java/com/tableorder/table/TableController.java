package com.tableorder.table;

import com.tableorder.security.AdminPrincipal;
import com.tableorder.table.dto.OrderHistoryResponse;
import com.tableorder.table.dto.TableSetupRequest;
import com.tableorder.table.dto.TableSetupResponse;
import com.tableorder.table.dto.TableSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public ResponseEntity<List<TableSummaryResponse>> getTables(
            @AuthenticationPrincipal AdminPrincipal principal) {
        return ResponseEntity.ok(tableService.getTablesByStore(principal.getStoreId()));
    }

    @PostMapping("/{tableId}/setup")
    public ResponseEntity<TableSetupResponse> setupTable(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long tableId,
            @Valid @RequestBody TableSetupRequest request) {
        return ResponseEntity.ok(tableService.setupTable(principal.getStoreId(), tableId, request));
    }

    @PostMapping("/{tableId}/complete")
    public ResponseEntity<Void> completeSession(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long tableId) {
        tableService.completeSession(principal.getStoreId(), tableId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tableId}/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            @AuthenticationPrincipal AdminPrincipal principal,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(tableService.getOrderHistory(principal.getStoreId(), tableId, from, to));
    }
}
