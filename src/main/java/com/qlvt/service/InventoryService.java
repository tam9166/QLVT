package com.qlvt.service;

import com.qlvt.entity.*;
import com.qlvt.enums.MovementType;
import com.qlvt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository movementRepository;
    private final AuditService auditService;
    private final InventorySyncService inventorySyncService;

    public InventoryService(MaterialRepository materialRepository,
                            MaterialBatchRepository batchRepository,
                            WarehouseRepository warehouseRepository,
                            StorageLocationRepository locationRepository,
                            SupplierRepository supplierRepository,
                            StockMovementRepository movementRepository,
                            AuditService auditService,
                            InventorySyncService inventorySyncService) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.movementRepository = movementRepository;
        this.auditService = auditService;
        this.inventorySyncService = inventorySyncService;
    }

    @Transactional
    public void receive(Long materialId, Long warehouseId, Long locationId, Long supplierId,
                        String batchNumber, LocalDate expiryDate, int quantity, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        Material material = materialRepository.findById(materialId).orElseThrow();
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        StorageLocation location = locationId == null ? null : locationRepository.findById(locationId).orElse(null);
        Supplier supplier = supplierId == null ? null : supplierRepository.findById(supplierId).orElse(null);

        int before = inventorySyncService.syncMaterialActualQuantity(material);
        MaterialBatch batch = batchRepository.findByMaterial_IdAndBatchNumber(materialId, batchNumber).orElseGet(MaterialBatch::new);
        batch.setMaterial(material);
        batch.setWarehouse(warehouse);
        batch.setLocation(location);
        batch.setSupplier(supplier);
        batch.setBatchNumber(batchNumber);
        batch.setExpiryDate(expiryDate);
        batch.setReceiptDate(LocalDate.now());
        batch.setQuantity(batch.getQuantity() + quantity);
        batchRepository.save(batch);

        int after = inventorySyncService.syncMaterialActualQuantity(material);
        movementRepository.save(movement(MovementType.IN, material, batch, warehouse, location, quantity, before, after, "RECEIPT", batchNumber, username));
        auditService.log(username, "RECEIVE_STOCK", "MATERIAL", material.getCode(), "Nhập " + quantity + " " + material.getUnit());
    }

    @Transactional
    public List<String> issueFefo(Long materialId, int quantity, String department, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }
        Material material = materialRepository.findById(materialId).orElseThrow();
        int before = inventorySyncService.syncMaterialActualQuantity(material);
        if (material.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Không đủ tồn khả dụng để xuất kho");
        }
        List<MaterialBatch> batches = batchRepository.findIssuableBatchesFefo(materialId, LocalDate.now());
        int remaining = quantity;
        int runningQuantity = before;
        List<String> allocations = new ArrayList<>();

        for (MaterialBatch batch : batches) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - take);
            batchRepository.save(batch);
            int afterTake = runningQuantity - take;
            movementRepository.save(movement(
                    MovementType.OUT,
                    material,
                    batch,
                    batch.getWarehouse(),
                    batch.getLocation(),
                    -take,
                    runningQuantity,
                    afterTake,
                    "ISSUE",
                    department,
                    username
            ));
            runningQuantity = afterTake;
            remaining -= take;
            allocations.add(locationLabel(batch) + " / lô " + batch.getBatchNumber() + ": " + take);
        }
        if (remaining > 0) {
            throw new IllegalStateException("Không có lô hợp lệ để xuất theo FEFO");
        }
        inventorySyncService.syncMaterialActualQuantity(material);
        auditService.log(username, "ISSUE_STOCK", "MATERIAL", material.getCode(), "Xuất " + quantity + " cho " + department + " theo FEFO: " + allocations);
        return allocations;
    }

    private String locationLabel(MaterialBatch batch) {
        String warehouseName = batch.getWarehouse() == null ? "Không rõ kho" : batch.getWarehouse().getName();
        String locationName = batch.getLocation() == null ? "Không rõ vị trí" : batch.getLocation().getName();
        return warehouseName + " / " + locationName;
    }

    private StockMovement movement(MovementType type, Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location,
                                   int quantity, int before, int after, String refType, String refCode, String username) {
        StockMovement movement = new StockMovement();
        movement.setMovementType(type);
        movement.setMaterial(material);
        movement.setBatch(batch);
        movement.setWarehouse(warehouse);
        movement.setLocation(location);
        movement.setQuantity(quantity);
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(refType);
        movement.setReferenceCode(refCode);
        movement.setCreatedBy(username);
        return movement;
    }
}
