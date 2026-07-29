package com.qlvt;

import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.BatchStatus;
import com.qlvt.repository.*;
import com.qlvt.service.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StockTransferWorkflowServiceTest {

    @Test
    void createTransferRejectsDestinationLocationFromAnotherWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse fromWarehouse = warehouse(1L);
        Warehouse toWarehouse = warehouse(2L);
        fixture.stubReferences(fromWarehouse, toWarehouse, location(9L, warehouse(3L), true, false));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> fixture.service.createTransfer(1L, 2L, 7L, 9L, 5, null, "tester"));

        assertEquals("Vị trí nhận không thuộc kho đến", error.getMessage());
        verify(fixture.stockTransferRepository, never()).save(any());
    }

    @Test
    void createTransferRejectsInactiveDestinationLocation() {
        Fixture fixture = new Fixture();
        Warehouse fromWarehouse = warehouse(1L);
        Warehouse toWarehouse = warehouse(2L);
        fixture.stubReferences(fromWarehouse, toWarehouse, location(9L, toWarehouse, false, false));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> fixture.service.createTransfer(1L, 2L, 7L, 9L, 5, null, "tester"));

        assertEquals("Vị trí nhận đã ngừng hoạt động", error.getMessage());
        verify(fixture.stockTransferRepository, never()).save(any());
    }

    private static Warehouse warehouse(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }

    private static StorageLocation location(Long id, Warehouse warehouse, boolean active, boolean deleted) {
        StorageLocation location = new StorageLocation();
        location.setId(id);
        location.setWarehouse(warehouse);
        location.setActive(active);
        location.setDeleted(deleted);
        return location;
    }

    private static class Fixture {
        private final InventoryCountRepository inventoryCountRepository = mock(InventoryCountRepository.class);
        private final InventoryCountLineRepository inventoryCountLineRepository = mock(InventoryCountLineRepository.class);
        private final StockAdjustmentRepository stockAdjustmentRepository = mock(StockAdjustmentRepository.class);
        private final StockTransferRepository stockTransferRepository = mock(StockTransferRepository.class);
        private final RecallOrderRepository recallOrderRepository = mock(RecallOrderRepository.class);
        private final RecallDepartmentResponseRepository recallResponseRepository = mock(RecallDepartmentResponseRepository.class);
        private final DestructionSlipRepository destructionSlipRepository = mock(DestructionSlipRepository.class);
        private final PurchaseRequestRepository purchaseRequestRepository = mock(PurchaseRequestRepository.class);
        private final PurchaseOrderRepository purchaseOrderRepository = mock(PurchaseOrderRepository.class);
        private final ReceiptRepository receiptRepository = mock(ReceiptRepository.class);
        private final MaterialRepository materialRepository = mock(MaterialRepository.class);
        private final MaterialBatchRepository batchRepository = mock(MaterialBatchRepository.class);
        private final WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        private final StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
        private final StockBalanceRepository balanceRepository = mock(StockBalanceRepository.class);
        private final StockMovementRepository movementRepository = mock(StockMovementRepository.class);
        private final IssueBatchAllocationRepository allocationRepository = mock(IssueBatchAllocationRepository.class);
        private final AppUserRepository userRepository = mock(AppUserRepository.class);
        private final AuditService auditService = mock(AuditService.class);
        private final NotificationService notificationService = mock(NotificationService.class);
        private final InventorySyncService inventorySyncService = mock(InventorySyncService.class);
        private final DepartmentStockService departmentStockService = mock(DepartmentStockService.class);
        private final Prompt3WorkflowService service = new Prompt3WorkflowService(
                inventoryCountRepository, inventoryCountLineRepository, stockAdjustmentRepository,
                stockTransferRepository, recallOrderRepository, recallResponseRepository,
                destructionSlipRepository, purchaseRequestRepository, purchaseOrderRepository,
                receiptRepository, materialRepository, batchRepository, warehouseRepository,
                locationRepository, supplierRepository, balanceRepository, movementRepository,
                allocationRepository, userRepository, auditService, notificationService,
                inventorySyncService, departmentStockService);

        private void stubReferences(Warehouse fromWarehouse, Warehouse toWarehouse, StorageLocation toLocation) {
            MaterialBatch batch = new MaterialBatch();
            batch.setExpiryDate(LocalDate.now().plusYears(1));
            batch.setStatus(BatchStatus.AVAILABLE);
            batch.setQuantity(10);
            StockBalance balance = new StockBalance();
            balance.setWarehouse(fromWarehouse);
            balance.setBatch(batch);
            balance.setActualQuantity(10);
            when(balanceRepository.findById(7L)).thenReturn(Optional.of(balance));
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(fromWarehouse));
            when(warehouseRepository.findById(2L)).thenReturn(Optional.of(toWarehouse));
            when(locationRepository.findById(9L)).thenReturn(Optional.of(toLocation));
        }
    }
}
