package com.qlvt.controller;

import com.qlvt.entity.Material;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.InventorySyncService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/qr")
public class MaterialQrController {
    private final MaterialRepository materialRepository;
    private final StockBalanceRepository balanceRepository;
    private final InventorySyncService inventorySyncService;

    public MaterialQrController(MaterialRepository materialRepository,
                                StockBalanceRepository balanceRepository,
                                InventorySyncService inventorySyncService) {
        this.materialRepository = materialRepository;
        this.balanceRepository = balanceRepository;
        this.inventorySyncService = inventorySyncService;
    }

    @GetMapping("/public/material/{code}")
    public String publicMaterial(@PathVariable String code, Model model) {
        model.addAttribute("material", findActiveMaterial(code));
        return "qr/public-material";
    }

    @GetMapping("/internal/material/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF','MANAGER')")
    public String internalMaterial(@PathVariable String code, Model model) {
        Material material = findActiveMaterial(code);
        model.addAttribute("material", material);
        model.addAttribute("syncedActualQuantity", inventorySyncService.calculateActualQuantity(material.getId()));
        model.addAttribute("balances", balanceRepository.findByMaterial_IdOrderByWarehouse_CodeAscLocation_CodeAsc(material.getId()));
        return "qr/internal-material";
    }

    private Material findActiveMaterial(String code) {
        Material material = materialRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư theo mã QR."));
        if (material.isDeleted()) {
            throw new ResourceNotFoundException("Vật tư này không còn trong danh mục.");
        }
        return material;
    }
}
