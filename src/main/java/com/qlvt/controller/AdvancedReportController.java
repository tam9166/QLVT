package com.qlvt.controller;

import com.qlvt.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports/advanced")
public class AdvancedReportController {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final InventoryCountRepository inventoryCountRepository;
    private final DestructionSlipRepository destructionSlipRepository;
    private final RecallOrderRepository recallOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AdvancedReportController(MaterialRepository materialRepository, MaterialBatchRepository batchRepository,
                                    StockBalanceRepository balanceRepository, StockMovementRepository movementRepository,
                                    InventoryCountRepository inventoryCountRepository, DestructionSlipRepository destructionSlipRepository,
                                    RecallOrderRepository recallOrderRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.inventoryCountRepository = inventoryCountRepository;
        this.destructionSlipRepository = destructionSlipRepository;
        this.recallOrderRepository = recallOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @GetMapping
    public String index(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("balances", balanceRepository.findAll());
        model.addAttribute("movements", movementRepository.findAll());
        model.addAttribute("expiringBatches", batchRepository.findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(today, today.plusDays(90)));
        model.addAttribute("expiredBatches", batchRepository.findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(today.minusYears(10), today));
        model.addAttribute("counts", inventoryCountRepository.findTop30ByOrderByCreatedAtDesc());
        model.addAttribute("destructions", destructionSlipRepository.findTop30ByOrderByCreatedAtDesc());
        model.addAttribute("recalls", recallOrderRepository.findTop30ByOrderByCreatedAtDesc());
        model.addAttribute("lateOrders", purchaseOrderRepository.findByExpectedDeliveryDateBefore(today));
        return "reports/advanced";
    }
}
