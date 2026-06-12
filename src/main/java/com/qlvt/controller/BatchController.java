package com.qlvt.controller;

import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.InventoryAlertService;
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

    public BatchController(MaterialBatchRepository batchRepository, StockBalanceRepository balanceRepository, InventoryAlertService alertService) {
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.alertService = alertService;
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
        return "batches/detail";
    }
}
