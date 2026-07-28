package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.enums.BatchStatus;
import com.qlvt.repository.MaterialBatchRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchExpiryServiceTest {
    @Test
    void expiresEligibleBatchesAndResynchronizesEachMaterialOnce() {
        MaterialBatch available = batch("LO-01", BatchStatus.AVAILABLE);
        MaterialBatch quarantined = batch("LO-02", BatchStatus.QUARANTINED);
        quarantined.setMaterial(available.getMaterial());

        MaterialBatchRepository repository = mock(MaterialBatchRepository.class);
        InventorySyncService syncService = mock(InventorySyncService.class);
        AuditService auditService = mock(AuditService.class);
        LocalDate today = LocalDate.of(2026, 7, 28);
        when(repository.findByExpiryDateLessThanEqualAndStatusIn(eq(today), anyList()))
                .thenReturn(List.of(available, quarantined));

        BatchExpiryService service = new BatchExpiryService(repository, syncService, auditService);

        assertThat(service.expireBatches(today)).isEqualTo(2);
        assertThat(available.getStatus()).isEqualTo(BatchStatus.EXPIRED);
        assertThat(quarantined.getStatus()).isEqualTo(BatchStatus.EXPIRED);
        verify(repository).saveAll(List.of(available, quarantined));
        verify(syncService).syncMaterialActualQuantity(available.getMaterial());
        verify(auditService).logChange(eq("SYSTEM"), eq("EXPIRE_BATCH"), eq("MATERIAL_BATCH"),
                eq("LO-01"), eq("AVAILABLE"), eq("EXPIRED"), eq("Tự động khóa lô đã hết hạn sử dụng 2026-07-27"));
    }

    private MaterialBatch batch(String number, BatchStatus status) {
        Material material = new Material();
        material.setCode("VT-01");
        MaterialBatch batch = new MaterialBatch();
        batch.setBatchNumber(number);
        batch.setMaterial(material);
        batch.setStatus(status);
        batch.setExpiryDate(LocalDate.of(2026, 7, 27));
        return batch;
    }
}
