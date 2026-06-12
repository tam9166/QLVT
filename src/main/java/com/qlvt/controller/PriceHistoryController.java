package com.qlvt.controller;

import com.qlvt.repository.MaterialPriceHistoryRepository;
import com.qlvt.repository.MaterialRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/price-histories")
public class PriceHistoryController {
    private final MaterialPriceHistoryRepository historyRepository;
    private final MaterialRepository materialRepository;

    public PriceHistoryController(MaterialPriceHistoryRepository historyRepository, MaterialRepository materialRepository) {
        this.historyRepository = historyRepository;
        this.materialRepository = materialRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", historyRepository.findTop50ByOrderByReceivedDateDescCreatedAtDesc());
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        return "price-histories/list";
    }

    @GetMapping("/material/{materialId}")
    public String byMaterial(@PathVariable Long materialId, Model model) {
        var material = materialRepository.findById(materialId).orElseThrow();
        model.addAttribute("material", material);
        model.addAttribute("items", historyRepository.findByMaterial_IdOrderByReceivedDateDescCreatedAtDesc(materialId));
        model.addAttribute("averagePrice", historyRepository.averagePrice(materialId));
        model.addAttribute("minPrice", historyRepository.minPrice(materialId));
        model.addAttribute("maxPrice", historyRepository.maxPrice(materialId));
        return "price-histories/material";
    }
}
