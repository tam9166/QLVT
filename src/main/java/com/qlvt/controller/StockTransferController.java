package com.qlvt.controller;

import com.qlvt.repository.*;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stock-transfers")
public class StockTransferController {
    private final StockTransferRepository repository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final StockBalanceRepository balanceRepository;
    private final Prompt3WorkflowService workflowService;

    public StockTransferController(StockTransferRepository repository, WarehouseRepository warehouseRepository, StorageLocationRepository locationRepository, StockBalanceRepository balanceRepository, Prompt3WorkflowService workflowService) {
        this.repository = repository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.balanceRepository = balanceRepository;
        this.workflowService = workflowService;
    }

    @GetMapping
    public String list(Model model) { model.addAttribute("items", repository.findTop30ByOrderByCreatedAtDesc()); return "stock-transfers/list"; }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("balances", balanceRepository.findAll());
        return "stock-transfers/form";
    }

    @PostMapping
    public String create(@RequestParam Long fromWarehouseId, @RequestParam Long toWarehouseId, @RequestParam Long balanceId,
                         @RequestParam Long toLocationId, @RequestParam int quantity, @RequestParam(required = false) String reason,
                         Authentication authentication) {
        var transfer = workflowService.createTransfer(fromWarehouseId, toWarehouseId, balanceId, toLocationId, quantity, reason, authentication.getName());
        return "redirect:/stock-transfers/" + transfer.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) { model.addAttribute("item", repository.findWithLinesById(id).orElseThrow()); return "stock-transfers/detail"; }
    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, Authentication authentication) { workflowService.submitTransfer(id, authentication.getName()); return "redirect:/stock-transfers/" + id; }
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication authentication) { workflowService.approveTransfer(id, authentication.getName()); return "redirect:/stock-transfers/" + id; }
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        workflowService.rejectTransfer(id, reason, authentication.getName());
        return "redirect:/stock-transfers/" + id;
    }
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam String reason, Authentication authentication) {
        workflowService.cancelTransfer(id, reason, authentication.getName());
        return "redirect:/stock-transfers/" + id;
    }
    @PostMapping("/{id}/transfer")
    public String transfer(@PathVariable Long id, Authentication authentication) { workflowService.executeTransfer(id, authentication.getName()); return "redirect:/stock-transfers/" + id; }
    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, Authentication authentication) { workflowService.receiveTransfer(id, authentication.getName()); return "redirect:/stock-transfers/" + id; }
}
