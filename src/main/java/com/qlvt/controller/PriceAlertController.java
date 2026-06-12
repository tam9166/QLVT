package com.qlvt.controller;

import com.qlvt.repository.PriceAlertRepository;
import com.qlvt.service.PriceHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/price-alerts")
public class PriceAlertController {
    private final PriceAlertRepository alertRepository;
    private final PriceHistoryService priceHistoryService;

    public PriceAlertController(PriceAlertRepository alertRepository, PriceHistoryService priceHistoryService) {
        this.alertRepository = alertRepository;
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", alertRepository.findTop50ByOrderByCreatedAtDesc());
        model.addAttribute("unresolvedCount", alertRepository.countByResolvedFalse());
        return "price-alerts/list";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id) {
        priceHistoryService.resolveAlert(id);
        return "redirect:/price-alerts";
    }
}
