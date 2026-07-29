package com.qlvt;

import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.Material;
import com.qlvt.enums.BatchStatus;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.BatchWorkflowService;
import com.qlvt.service.InventorySyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchWorkflowServiceTest {
    private MaterialBatchRepository batchRepository;
    private StockBalanceRepository stockBalanceRepository;
    private InventorySyncService inventorySyncService;
    private BatchWorkflowService service;

    @BeforeEach
    void setUp() {
        batchRepository = mock(MaterialBatchRepository.class);
        stockBalanceRepository = mock(StockBalanceRepository.class);
        inventorySyncService = mock(InventorySyncService.class);
        service = new BatchWorkflowService(
                batchRepository, stockBalanceRepository, inventorySyncService, mock(AuditService.class));
    }

    @Test
    void cannotQuarantineBatchWithReservedOrPendingIssueQuantity() {
        MaterialBatch batch = new MaterialBatch();
        batch.setId(12L);
        batch.setBatchNumber("LO-001");
        batch.setStatus(BatchStatus.AVAILABLE);
        when(batchRepository.findById(12L)).thenReturn(Optional.of(batch));
        when(stockBalanceRepository.hasCommittedQuantityForBatch(12L)).thenReturn(true);

        assertThatThrownBy(() -> service.quarantine(12L, "Quality concern", "warehouse"))
                .isInstanceOf(IllegalStateException.class);

        verify(batchRepository, never()).save(batch);
    }

    @Test
    void quarantineResynchronizesMaterialActualQuantity() {
        Material material = new Material();
        MaterialBatch batch = batch(12L, material, BatchStatus.AVAILABLE);
        when(batchRepository.findById(12L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(batch)).thenReturn(batch);

        service.quarantine(12L, "Quality concern", "warehouse");

        verify(inventorySyncService).syncMaterialActualQuantity(material);
    }

    @Test
    void releaseResynchronizesMaterialActualQuantity() {
        Material material = new Material();
        MaterialBatch batch = batch(12L, material, BatchStatus.QUARANTINED);
        when(batchRepository.findById(12L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(batch)).thenReturn(batch);

        service.release(12L, "Quality accepted", "warehouse");

        verify(inventorySyncService).syncMaterialActualQuantity(material);
    }

    private MaterialBatch batch(Long id, Material material, BatchStatus status) {
        MaterialBatch batch = new MaterialBatch();
        batch.setId(id);
        batch.setMaterial(material);
        batch.setBatchNumber("LO-001");
        batch.setStatus(status);
        return batch;
    }
}
