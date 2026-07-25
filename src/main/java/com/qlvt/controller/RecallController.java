package com.qlvt.controller;

import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.RecallDepartmentResponseRepository;
import com.qlvt.repository.RecallOrderRepository;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recalls")
public class RecallController {
    private final RecallOrderRepository repository;
    private final MaterialBatchRepository batchRepository;
    private final RecallDepartmentResponseRepository responseRepository;
    private final Prompt3WorkflowService workflowService;

    public RecallController(RecallOrderRepository repository, MaterialBatchRepository batchRepository,
                            RecallDepartmentResponseRepository responseRepository,
                            Prompt3WorkflowService workflowService) {
        this.repository = repository;
        this.batchRepository = batchRepository;
        this.responseRepository = responseRepository;
        this.workflowService = workflowService;
    }

    @GetMapping
    public String list(Model model) { model.addAttribute("items", repository.findTop30ByOrderByCreatedAtDesc()); return "recalls/list"; }
    @GetMapping("/new")
    public String form(Model model) { model.addAttribute("batches", batchRepository.findAll()); return "recalls/form"; }
    @PostMapping
    public String create(@RequestParam Long batchId, @RequestParam String reason, Authentication authentication) {
        var recall = workflowService.createRecall(batchId, reason, authentication.getName());
        return "redirect:/recalls/" + recall.getId();
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("item", repository.findDetailById(id).orElseThrow());
        model.addAttribute("responses", responseRepository.findByRecallOrder_IdOrderByRespondedAtDesc(id));
        return "recalls/detail";
    }
    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, Authentication authentication) { workflowService.activateRecall(id, authentication.getName()); return "redirect:/recalls/" + id; }
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        workflowService.cancelRecall(id, reason, authentication.getName());
        return "redirect:/recalls/" + id;
    }
    @PostMapping("/{id}/department-response")
    public String response(@PathVariable Long id, @RequestParam String department, @RequestParam int remainingQuantity,
                           @RequestParam int usedQuantity, @RequestParam int returnedQuantity, @RequestParam(required = false) String note,
                           Authentication authentication) {
        workflowService.respondRecall(id, department, remainingQuantity, usedQuantity, returnedQuantity, note, authentication.getName());
        return "redirect:/recalls/" + id;
    }
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication authentication) { workflowService.completeRecall(id, authentication.getName()); return "redirect:/recalls/" + id; }
}
