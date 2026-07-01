package com.qlvt;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.StockMovement;
import com.qlvt.entity.StorageLocation;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.BatchStatus;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.StockMovementRepository;
import com.qlvt.repository.StorageLocationRepository;
import com.qlvt.repository.SupplierRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.InventoryService;
import com.qlvt.service.InventorySyncService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    @Test
    void issueFefoUpdatesBalancesBatchesAndMovementDetailsPerAllocation() {
        Material material = material();
        Warehouse warehouse = warehouse();
        StorageLocation locationA = location(warehouse, "A1");
        StorageLocation locationB = location(warehouse, "B1");
        MaterialBatch batchA = batch(material, warehouse, locationA, "B001", 20, LocalDate.now().plusMonths(1));
        MaterialBatch batchB = batch(material, warehouse, locationB, "B002", 30, LocalDate.now().plusMonths(2));
        StockBalance balanceA = balance(material, warehouse, locationA, batchA, 20);
        StockBalance balanceB = balance(material, warehouse, locationB, batchB, 30);

        MaterialRepository materialRepository = mock(MaterialRepository.class);
        StockBalanceRepository balanceRepository = mock(StockBalanceRepository.class);
        MaterialBatchRepository batchRepository = mock(MaterialBatchRepository.class);
        StockMovementRepository movementRepository = mock(StockMovementRepository.class);
        InventorySyncService syncService = mock(InventorySyncService.class);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(balanceRepository.findAvailableFefo(1L, LocalDate.now())).thenReturn(List.of(balanceA, balanceB));
        when(syncService.syncMaterialActualQuantity(material))
                .thenAnswer(invocation -> {
                    material.setActualQuantity(50);
                    return 50;
                })
                .thenAnswer(invocation -> {
                    material.setActualQuantity(15);
                    return 15;
                });

        InventoryService service = new InventoryService(
                materialRepository,
                batchRepository,
                mock(WarehouseRepository.class),
                mock(StorageLocationRepository.class),
                mock(SupplierRepository.class),
                balanceRepository,
                movementRepository,
                mock(AuditService.class),
                syncService
        );

        List<String> allocations = service.issueFefo(1L, 35, "Khoa Cap cuu", "tester");

        assertThat(balanceA.getActualQuantity()).isZero();
        assertThat(balanceB.getActualQuantity()).isEqualTo(15);
        assertThat(batchA.getQuantity()).isZero();
        assertThat(batchB.getQuantity()).isEqualTo(15);
        assertThat(allocations).containsExactly(
                "Kho chinh / Ke A1 / lô B001: 20",
                "Kho chinh / Ke B1 / lô B002: 15"
        );

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository, times(2)).save(movementCaptor.capture());
        List<StockMovement> movements = movementCaptor.getAllValues();
        assertThat(movements).extracting(StockMovement::getQuantity).containsExactly(-20, -15);
        assertThat(movements.get(0).getBatch()).isSameAs(batchA);
        assertThat(movements.get(0).getWarehouse()).isSameAs(warehouse);
        assertThat(movements.get(0).getLocation()).isSameAs(locationA);
        assertThat(movements.get(1).getBatch()).isSameAs(batchB);
        assertThat(movements.get(1).getWarehouse()).isSameAs(warehouse);
        assertThat(movements.get(1).getLocation()).isSameAs(locationB);
    }

    private Material material() {
        Material material = new Material();
        material.setId(1L);
        material.setCode("VT001");
        material.setName("Khau trang");
        material.setUnit("cai");
        return material;
    }

    private Warehouse warehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("KHO1");
        warehouse.setName("Kho chinh");
        return warehouse;
    }

    private StorageLocation location(Warehouse warehouse, String code) {
        StorageLocation location = new StorageLocation();
        location.setId("A1".equals(code) ? 1L : 2L);
        location.setCode(code);
        location.setName("Ke " + code);
        location.setWarehouse(warehouse);
        return location;
    }

    private MaterialBatch batch(Material material, Warehouse warehouse, StorageLocation location,
                                String batchNumber, int quantity, LocalDate expiryDate) {
        MaterialBatch batch = new MaterialBatch();
        batch.setId("B001".equals(batchNumber) ? 1L : 2L);
        batch.setMaterial(material);
        batch.setWarehouse(warehouse);
        batch.setLocation(location);
        batch.setBatchNumber(batchNumber);
        batch.setReceiptDate(LocalDate.now().minusDays(5));
        batch.setExpiryDate(expiryDate);
        batch.setInitialQuantity(quantity);
        batch.setQuantity(quantity);
        batch.setStatus(BatchStatus.AVAILABLE);
        return batch;
    }

    private StockBalance balance(Material material, Warehouse warehouse, StorageLocation location,
                                 MaterialBatch batch, int actualQuantity) {
        StockBalance balance = new StockBalance();
        balance.setId(batch.getId());
        balance.setMaterial(material);
        balance.setWarehouse(warehouse);
        balance.setLocation(location);
        balance.setBatch(batch);
        balance.setActualQuantity(actualQuantity);
        return balance;
    }
}
