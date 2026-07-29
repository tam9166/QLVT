package com.qlvt;

import com.qlvt.controller.BatchController;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.AuditLogRepository;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.BatchWorkflowService;
import com.qlvt.service.InventoryAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchControllerTest {
    private MaterialBatchRepository batchRepository;
    private StockBalanceRepository balanceRepository;
    private BatchController controller;

    @BeforeEach
    void setUp() {
        batchRepository = mock(MaterialBatchRepository.class);
        balanceRepository = mock(StockBalanceRepository.class);
        controller = new BatchController(batchRepository, balanceRepository,
                mock(InventoryAlertService.class), mock(BatchWorkflowService.class),
                mock(AuditLogRepository.class));
    }

    @Test
    void detailLoadsOnlyBalancesForRequestedBatch() {
        MaterialBatch batch = new MaterialBatch();
        batch.setId(42L);
        when(batchRepository.findById(42L)).thenReturn(Optional.of(batch));
        when(balanceRepository.findByBatch_IdOrderByWarehouse_CodeAscLocation_CodeAsc(42L))
                .thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        assertThat(controller.detail(42L, model)).isEqualTo("batches/detail");

        verify(balanceRepository).findByBatch_IdOrderByWarehouse_CodeAscLocation_CodeAsc(42L);
        verify(balanceRepository, never()).findAll();
        assertThat(model.getAttribute("batch")).isSameAs(batch);
    }

    @Test
    void detailReportsMissingBatchAsNotFound() {
        when(batchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.detail(99L, new ConcurrentModel()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(balanceRepository, never()).findByBatch_IdOrderByWarehouse_CodeAscLocation_CodeAsc(99L);
    }
}
