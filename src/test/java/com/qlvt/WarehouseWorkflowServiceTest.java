package com.qlvt;

import com.qlvt.entity.Material;
import com.qlvt.entity.Receipt;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.repository.*;
import com.qlvt.service.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WarehouseWorkflowServiceTest {

    @Test
    void createReceiptRejectsLocationFromAnotherWarehouse() {
        Fixture fixture = new Fixture();
        Warehouse selectedWarehouse = warehouse(1L);
        StorageLocation foreignLocation = location(9L, warehouse(2L), true, false);
        fixture.stubReceiptReferences(selectedWarehouse, foreignLocation);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> fixture.service.createReceipt(1L, 1L, 9L, null, LocalDate.now(),
                        LocalDate.now().plusYears(1), 10, BigDecimal.ONE, null, "tester"));

        assertEquals("Vị trí lưu trữ không thuộc kho đã chọn", error.getMessage());
        verifyNoInteractions(fixture.receiptRepository);
    }

    @Test
    void updateReceiptRejectsInactiveLocation() {
        Fixture fixture = new Fixture();
        Warehouse selectedWarehouse = warehouse(1L);
        StorageLocation inactiveLocation = location(9L, selectedWarehouse, false, false);
        Receipt draft = new Receipt();
        when(fixture.receiptRepository.findById(7L)).thenReturn(Optional.of(draft));
        fixture.stubReceiptReferences(selectedWarehouse, inactiveLocation);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> fixture.service.updateReceiptDraft(7L, 1L, 1L, 9L, null, LocalDate.now(),
                        LocalDate.now().plusYears(1), 10, BigDecimal.ONE, null, "tester"));

        assertEquals("Vị trí lưu trữ đã ngừng hoạt động", error.getMessage());
        verify(fixture.receiptRepository, never()).save(any());
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
        private final MaterialRepository materialRepository = mock(MaterialRepository.class);
        private final MaterialBatchRepository batchRepository = mock(MaterialBatchRepository.class);
        private final WarehouseRepository warehouseRepository = mock(WarehouseRepository.class);
        private final StorageLocationRepository locationRepository = mock(StorageLocationRepository.class);
        private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
        private final MaterialRequestRepository requestRepository = mock(MaterialRequestRepository.class);
        private final StockBalanceRepository balanceRepository = mock(StockBalanceRepository.class);
        private final StockReservationRepository reservationRepository = mock(StockReservationRepository.class);
        private final RequestApprovalLogRepository approvalLogRepository = mock(RequestApprovalLogRepository.class);
        private final ReceiptRepository receiptRepository = mock(ReceiptRepository.class);
        private final IssueSlipRepository issueSlipRepository = mock(IssueSlipRepository.class);
        private final StockMovementRepository movementRepository = mock(StockMovementRepository.class);
        private final AuditService auditService = mock(AuditService.class);
        private final DepartmentStockService departmentStockService = mock(DepartmentStockService.class);
        private final PriceHistoryService priceHistoryService = mock(PriceHistoryService.class);
        private final InventoryAlertService inventoryAlertService = mock(InventoryAlertService.class);
        private final InventorySyncService inventorySyncService = mock(InventorySyncService.class);
        private final WarehouseWorkflowService service = new WarehouseWorkflowService(
                materialRepository, batchRepository, warehouseRepository, locationRepository, supplierRepository,
                requestRepository, balanceRepository, reservationRepository, approvalLogRepository, receiptRepository,
                issueSlipRepository, movementRepository, auditService, departmentStockService, priceHistoryService,
                inventoryAlertService, inventorySyncService);

        private void stubReceiptReferences(Warehouse warehouse, StorageLocation location) {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(new Material()));
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
            when(locationRepository.findById(9L)).thenReturn(Optional.of(location));
        }
    }
}
