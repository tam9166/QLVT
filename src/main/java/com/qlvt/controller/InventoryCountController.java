package com.qlvt.controller;

import com.qlvt.repository.InventoryCountRepository;
import com.qlvt.repository.StockAdjustmentRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/inventory-counts")
public class InventoryCountController {
    private final InventoryCountRepository inventoryCountRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final Prompt3WorkflowService workflowService;

    public InventoryCountController(InventoryCountRepository inventoryCountRepository, WarehouseRepository warehouseRepository,
                                    StockAdjustmentRepository stockAdjustmentRepository, Prompt3WorkflowService workflowService) {
        this.inventoryCountRepository = inventoryCountRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.workflowService = workflowService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", inventoryCountRepository.findTop30ByOrderByCreatedAtDesc());
        return "inventory-counts/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("warehouses", warehouseRepository.findAll());
        return "inventory-counts/form";
    }

    @PostMapping
    public String create(@RequestParam Long warehouseId, @RequestParam(required = false) String note, Authentication authentication) {
        var count = workflowService.createInventoryCount(warehouseId, note, authentication.getName());
        return "redirect:/inventory-counts/" + count.getId() + "/lines";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("item", inventoryCountRepository.findWithLinesById(id).orElseThrow());
        stockAdjustmentRepository.findFirstByInventoryCount_IdOrderByCreatedAtDesc(id)
                .ifPresent(adjustment -> model.addAttribute("adjustment", adjustment));
        return "inventory-counts/detail";
    }

    @GetMapping("/{id}/lines")
    public String lines(@PathVariable Long id, Model model) {
        model.addAttribute("item", inventoryCountRepository.findWithLinesById(id).orElseThrow());
        return "inventory-counts/lines";
    }

    @PostMapping("/{id}/lines")
    public String updateLines(@PathVariable Long id, @RequestParam Map<String, String> params, Authentication authentication) {
        Map<Long, Integer> quantities = new HashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("actual_") && value != null && !value.isBlank()) {
                quantities.put(Long.parseLong(key.substring(7)), Integer.parseInt(value));
            }
        });
        workflowService.updateInventoryCountLines(id, quantities, authentication.getName());
        return "redirect:/inventory-counts/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication authentication) {
        workflowService.completeInventoryCount(id, authentication.getName());
        return "redirect:/inventory-counts/" + id;
    }

    @PostMapping("/{id}/create-adjustment")
    public String createAdjustment(@PathVariable Long id, Authentication authentication) {
        var adjustment = workflowService.createAdjustmentFromCount(id, authentication.getName());
        return "redirect:/stock-adjustments/" + adjustment.getId();
    }
}
