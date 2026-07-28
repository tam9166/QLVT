package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.enums.BatchStatus;
import com.qlvt.repository.MaterialBatchRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BatchExpiryService {
    private static final List<BatchStatus> EXPIRABLE_STATUSES =
            List.of(BatchStatus.AVAILABLE, BatchStatus.QUARANTINED);

    private final MaterialBatchRepository batchRepository;
    private final InventorySyncService inventorySyncService;
    private final AuditService auditService;

    public BatchExpiryService(MaterialBatchRepository batchRepository,
                              InventorySyncService inventorySyncService,
                              AuditService auditService) {
        this.batchRepository = batchRepository;
        this.inventorySyncService = inventorySyncService;
        this.auditService = auditService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void expireBatchesOnStartup() {
        expireBatches(LocalDate.now());
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void expireBatchesBySchedule() {
        expireBatches(LocalDate.now());
    }

    int expireBatches(LocalDate today) {
        List<MaterialBatch> expired = batchRepository
                .findByExpiryDateLessThanEqualAndStatusIn(today, EXPIRABLE_STATUSES);
        Set<Material> affectedMaterials = new LinkedHashSet<>();
        LocalDateTime changedAt = LocalDateTime.now();

        for (MaterialBatch batch : expired) {
            BatchStatus oldStatus = batch.getStatus();
            batch.setStatus(BatchStatus.EXPIRED);
            batch.setUpdatedAt(changedAt);
            affectedMaterials.add(batch.getMaterial());
            auditService.logChange("SYSTEM", "EXPIRE_BATCH", "MATERIAL_BATCH", batch.getBatchNumber(),
                    oldStatus.name(), BatchStatus.EXPIRED.name(),
                    "Tự động khóa lô đã hết hạn sử dụng " + batch.getExpiryDate());
        }

        if (!expired.isEmpty()) {
            batchRepository.saveAll(expired);
            affectedMaterials.forEach(inventorySyncService::syncMaterialActualQuantity);
        }
        return expired.size();
    }
}
