package com.qlvt.controller;

import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.AuditLogRepository;
import com.qlvt.service.InventoryAlertService;
import com.qlvt.service.BatchWorkflowService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/batches")
public class BatchController {
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final InventoryAlertService alertService;
    private final BatchWorkflowService workflowService;
    private final AuditLogRepository auditLogRepository;

    public BatchController(MaterialBatchRepository batchRepository, StockBalanceRepository balanceRepository,
                           InventoryAlertService alertService, BatchWorkflowService workflowService,
                           AuditLogRepository auditLogRepository) {
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.alertService = alertService;
        this.workflowService = workflowService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "90") int expiringDays, Model model) {
        model.addAttribute("batches", batchRepository.findAll());
        model.addAttribute("expiring", batchRepository.findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate.now(), LocalDate.now().plusDays(expiringDays)));
        model.addAttribute("expiringDays", expiringDays);
        model.addAttribute("alertService", alertService);
        return "batches/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var batch = batchRepository.findById(id).orElseThrow();
        model.addAttribute("batch", batch);
        model.addAttribute("balances", balanceRepository.findAll().stream()
                .filter(balance -> balance.getBatch().getId().equals(id)).toList());
        model.addAttribute("hasCommittedQuantity", balanceRepository.hasCommittedQuantityForBatch(id));
        model.addAttribute("history", auditLogRepository
                .findByEntityNameAndEntityIdOrderByCreatedAtDesc("MATERIAL_BATCH", batch.getBatchNumber()));
        return "batches/detail";
    }

    @PostMapping("/{id}/quarantine")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String quarantine(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        workflowService.quarantine(id, reason, authentication.getName());
        return "redirect:/batches/" + id;
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
    public String release(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        workflowService.release(id, reason, authentication.getName());
        return "redirect:/batches/" + id;
    }
}
