package com.qlvt.service;

import com.qlvt.dto.AdvancedReportRows.BatchExpiryRow;
import com.qlvt.dto.AdvancedReportRows.CodeStatusRow;
import com.qlvt.dto.AdvancedReportRows.InventoryCountRow;
import com.qlvt.dto.AdvancedReportRows.StockBalanceRow;
import com.qlvt.entity.InventoryCount;
import com.qlvt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdvancedReportService {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final InventoryCountRepository inventoryCountRepository;
    private final InventoryCountLineRepository inventoryCountLineRepository;
    private final DestructionSlipRepository destructionSlipRepository;
    private final RecallOrderRepository recallOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AdvancedReportService(MaterialRepository materialRepository,
                                 MaterialBatchRepository batchRepository,
                                 StockBalanceRepository balanceRepository,
                                 StockMovementRepository movementRepository,
                                 InventoryCountRepository inventoryCountRepository,
                                 InventoryCountLineRepository inventoryCountLineRepository,
                                 DestructionSlipRepository destructionSlipRepository,
                                 RecallOrderRepository recallOrderRepository,
                                 PurchaseOrderRepository purchaseOrderRepository) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.inventoryCountRepository = inventoryCountRepository;
        this.inventoryCountLineRepository = inventoryCountLineRepository;
        this.destructionSlipRepository = destructionSlipRepository;
        this.recallOrderRepository = recallOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional(readOnly = true)
    public AdvancedReportView load() {
        LocalDate today = LocalDate.now();
        List<StockBalanceRow> balances = balanceRepository.findAll().stream()
                .map(balance -> new StockBalanceRow(
                        balance.getMaterial().getCode() + " - " + balance.getMaterial().getName(),
                        balance.getBatch().getBatchNumber(),
                        balance.getWarehouse().getName(),
                        balance.getActualQuantity(),
                        balance.getAvailableQuantity()))
                .toList();
        List<BatchExpiryRow> expiringBatches = batchRepository.findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(today, today.plusDays(90)).stream()
                .map(batch -> new BatchExpiryRow(batch.getBatchNumber(), batch.getMaterial().getName(), batch.getExpiryDate(), batch.getQuantity()))
                .toList();
        List<BatchExpiryRow> expiredBatches = batchRepository.findTop10ByExpiryDateBetweenOrderByExpiryDateAsc(today.minusYears(10), today.minusDays(1)).stream()
                .map(batch -> new BatchExpiryRow(batch.getBatchNumber(), batch.getMaterial().getName(), batch.getExpiryDate(), batch.getQuantity()))
                .toList();
        Map<Long, Integer> lineCounts = inventoryCountLineRepository.findAll().stream()
                .collect(Collectors.groupingBy(line -> line.getInventoryCount().getId(), Collectors.summingInt(line -> 1)));
        List<InventoryCountRow> counts = inventoryCountRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(count -> countRow(count, lineCounts))
                .toList();
        List<CodeStatusRow> destructions = destructionSlipRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(item -> new CodeStatusRow(item.getDestructionCode(), item.getStatus().toString()))
                .toList();
        List<CodeStatusRow> recalls = recallOrderRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(item -> new CodeStatusRow(item.getRecallCode(), item.getStatus().toString()))
                .toList();
        List<CodeStatusRow> lateOrders = purchaseOrderRepository.findByExpectedDeliveryDateBefore(today).stream()
                .map(item -> new CodeStatusRow(item.getOrderCode(), item.getStatus().toString()))
                .toList();
        return new AdvancedReportView(
                materialRepository.count(),
                balances.size(),
                movementRepository.count(),
                expiringBatches,
                expiredBatches,
                balances,
                counts,
                destructions,
                recalls,
                lateOrders);
    }

    private InventoryCountRow countRow(InventoryCount count, Map<Long, Integer> lineCounts) {
        return new InventoryCountRow(
                count.getCountCode(),
                count.getWarehouse().getName(),
                count.getStatus().toString(),
                lineCounts.getOrDefault(count.getId(), 0));
    }

    public record AdvancedReportView(long materialCount,
                                     int balanceCount,
                                     long movementCount,
                                     List<BatchExpiryRow> expiringBatches,
                                     List<BatchExpiryRow> expiredBatches,
                                     List<StockBalanceRow> balances,
                                     List<InventoryCountRow> counts,
                                     List<CodeStatusRow> destructions,
                                     List<CodeStatusRow> recalls,
                                     List<CodeStatusRow> lateOrders) {
    }
}
