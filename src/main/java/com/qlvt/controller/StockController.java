package com.qlvt.controller;

import com.qlvt.repository.*;
import com.qlvt.service.InventoryService;
import com.qlvt.service.InventoryAlertService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/stock")
public class StockController {
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final InventoryAlertService alertService;
    private final InventoryService inventoryService;

    public StockController(MaterialRepository materialRepository,
                           WarehouseRepository warehouseRepository,
                           StorageLocationRepository locationRepository,
                           SupplierRepository supplierRepository,
                           MaterialBatchRepository batchRepository,
                           StockBalanceRepository balanceRepository,
                           InventoryAlertService alertService,
                           InventoryService inventoryService) {
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.alertService = alertService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String stock(Model model) {
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("batches", batchRepository.findAll());
        model.addAttribute("balances", balanceRepository.findAll());
        model.addAttribute("alertService", alertService);
        return "stock/list";
    }

    @GetMapping("/receive")
    public String receiveForm(Model model) {
        sharedFormData(model);
        return "stock/receive";
    }

    @PostMapping("/receive")
    public String receive(@RequestParam Long materialId, @RequestParam Long warehouseId,
                          @RequestParam(required = false) Long locationId,
                          @RequestParam(required = false) Long supplierId,
                          @RequestParam String batchNumber,
                          @RequestParam(required = false) LocalDate expiryDate,
                          @RequestParam int quantity,
                          Authentication authentication) {
        inventoryService.receive(materialId, warehouseId, locationId, supplierId, batchNumber, expiryDate, quantity, authentication.getName());
        return "redirect:/stock";
    }

    @GetMapping("/issue")
    public String issueForm(Model model) {
        sharedFormData(model);
        return "stock/issue";
    }

    @PostMapping("/issue")
    public String issue(@RequestParam Long materialId, @RequestParam int quantity,
                        @RequestParam String department, Authentication authentication) {
        inventoryService.issueFefo(materialId, quantity, department, authentication.getName());
        return "redirect:/stock";
    }

    private void sharedFormData(Model model) {
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
    }
}
