package com.qlvt.controller;

import com.qlvt.repository.StockAdjustmentRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.service.AttachmentService;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stock-adjustments")
public class StockAdjustmentController {
    private final StockAdjustmentRepository repository;
    private final StockBalanceRepository balanceRepository;
    private final Prompt3WorkflowService workflowService;
    private final AttachmentService attachmentService;

    public StockAdjustmentController(StockAdjustmentRepository repository, StockBalanceRepository balanceRepository, Prompt3WorkflowService workflowService, AttachmentService attachmentService) {
        this.repository = repository;
        this.balanceRepository = balanceRepository;
        this.workflowService = workflowService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public String list(Model model) { model.addAttribute("items", repository.findTop30ByOrderByCreatedAtDesc()); return "stock-adjustments/list"; }

    @GetMapping("/new")
    public String form(Model model) { model.addAttribute("balances", balanceRepository.findAll()); return "stock-adjustments/form"; }

    @PostMapping
    public String create(@RequestParam Long balanceId, @RequestParam int actualQuantity, @RequestParam(required = false) String reason, Authentication authentication) {
        var adjustment = workflowService.createManualAdjustment(balanceId, actualQuantity, reason, authentication.getName());
        return "redirect:/stock-adjustments/" + adjustment.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) { model.addAttribute("item", repository.findWithLinesById(id).orElseThrow()); model.addAttribute("attachments", attachmentService.list(AttachmentReferenceType.STOCK_ADJUSTMENT, id)); model.addAttribute("attachmentType", AttachmentReferenceType.STOCK_ADJUSTMENT); return "stock-adjustments/detail"; }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, Authentication authentication) { workflowService.submitAdjustment(id, authentication.getName()); return "redirect:/stock-adjustments/" + id; }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication authentication) { workflowService.approveAdjustment(id, authentication.getName()); return "redirect:/stock-adjustments/" + id; }

    @PostMapping("/{id}/approve-accountant")
    public String approveAccountant(@PathVariable Long id, Authentication authentication) { workflowService.approveAdjustmentByAccountant(id, authentication.getName()); return "redirect:/stock-adjustments/" + id; }
}
