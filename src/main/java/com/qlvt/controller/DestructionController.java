package com.qlvt.controller;

import com.qlvt.enums.DestructionReason;
import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.repository.DestructionSlipRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.AttachmentService;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/destructions")
public class DestructionController {
    private final DestructionSlipRepository repository;
    private final StockBalanceRepository balanceRepository;
    private final Prompt3WorkflowService workflowService;
    private final AttachmentService attachmentService;

    public DestructionController(DestructionSlipRepository repository, StockBalanceRepository balanceRepository, Prompt3WorkflowService workflowService, AttachmentService attachmentService) {
        this.repository = repository;
        this.balanceRepository = balanceRepository;
        this.workflowService = workflowService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public String list(Model model) { model.addAttribute("items", repository.findTop30ByOrderByCreatedAtDesc()); return "destructions/list"; }
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("balances", balanceRepository.findAll());
        model.addAttribute("reasons", DestructionReason.values());
        return "destructions/form";
    }
    @PostMapping
    public String create(@RequestParam Long balanceId, @RequestParam int quantity, @RequestParam DestructionReason reason,
                         @RequestParam(required = false) String note, Authentication authentication) {
        var slip = workflowService.createDestruction(balanceId, quantity, reason, note, authentication.getName());
        return "redirect:/destructions/" + slip.getId();
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) { model.addAttribute("item", repository.findWithLinesById(id).orElseThrow()); model.addAttribute("attachments", attachmentService.list(AttachmentReferenceType.DESTRUCTION_SLIP, id)); model.addAttribute("attachmentType", AttachmentReferenceType.DESTRUCTION_SLIP); return "destructions/detail"; }
    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, Authentication authentication) { workflowService.submitDestruction(id, authentication.getName()); return "redirect:/destructions/" + id; }
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication authentication) { workflowService.approveDestruction(id, authentication.getName()); return "redirect:/destructions/" + id; }
    @PostMapping("/{id}/approve-accountant")
    public String approveAccountant(@PathVariable Long id, Authentication authentication) { workflowService.approveDestructionByAccountant(id, authentication.getName()); return "redirect:/destructions/" + id; }
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason, Authentication authentication) { workflowService.rejectDestruction(id, reason, authentication.getName()); return "redirect:/destructions/" + id; }
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam String reason, Authentication authentication) { workflowService.cancelDestruction(id, reason, authentication.getName()); return "redirect:/destructions/" + id; }
    @PostMapping("/{id}/destroy")
    public String destroy(@PathVariable Long id, Authentication authentication) { workflowService.destroy(id, authentication.getName()); return "redirect:/destructions/" + id; }
}
