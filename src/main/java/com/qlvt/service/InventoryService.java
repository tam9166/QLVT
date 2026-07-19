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
    private final StockBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final AuditService auditService;
    private final InventorySyncService inventorySyncService;

    public InventoryService(MaterialRepository materialRepository,
                            MaterialBatchRepository batchRepository,
                            WarehouseRepository warehouseRepository,
                            StorageLocationRepository locationRepository,
                            SupplierRepository supplierRepository,
                            StockBalanceRepository balanceRepository,
                            StockMovementRepository movementRepository,
                            AuditService auditService,
                            InventorySyncService inventorySyncService) {
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.balanceRepository = balanceRepository;
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
        StorageLocation location = resolveReceiveLocation(warehouseId, locationId);
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
        if (batch.getInitialQuantity() == 0) {
            batch.setInitialQuantity(quantity);
        } else {
            batch.setInitialQuantity(batch.getInitialQuantity() + quantity);
        }
        batch.setQuantity(batch.getQuantity() + quantity);
        batchRepository.save(batch);

        StockBalance balance = findOrCreateBalance(material, batch, warehouse, location);
        balance.setActualQuantity(balance.getActualQuantity() + quantity);
        balance.validate();
        balanceRepository.save(balance);

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
        int remaining = quantity;
        int runningQuantity = before;
        List<String> allocations = new ArrayList<>();

        for (StockBalance balance : balanceRepository.findAvailableFefo(materialId, LocalDate.now())) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(balance.getAvailableQuantity(), remaining);
            if (take <= 0) {
                continue;
            }
            MaterialBatch batch = balance.getBatch();
            balance.setActualQuantity(balance.getActualQuantity() - take);
            balance.validate();
            balanceRepository.save(balance);

            batch.setQuantity(batch.getQuantity() - take);
            batchRepository.save(batch);
            int afterTake = runningQuantity - take;
            movementRepository.save(movement(
                    MovementType.OUT,
                    material,
                    batch,
                    balance.getWarehouse(),
                    balance.getLocation(),
                    -take,
                    runningQuantity,
                    afterTake,
                    "ISSUE",
                    department,
                    username
            ));
            runningQuantity = afterTake;
            remaining -= take;
            allocations.add(balance.getWarehouse().getName()
                    + " / " + balance.getLocation().getName()
                    + " / lô " + batch.getBatchNumber()
                    + ": " + take);
        }
        if (remaining > 0) {
            throw new IllegalStateException("Không có lô hợp lệ để xuất theo FEFO");
        }
        int after = inventorySyncService.syncMaterialActualQuantity(material);
        auditService.log(username, "ISSUE_STOCK", "MATERIAL", material.getCode(), "Xuất " + quantity + " cho " + department + " theo FEFO: " + allocations);
        return allocations;
    }

    private StorageLocation resolveReceiveLocation(Long warehouseId, Long locationId) {
        if (locationId != null) {
            return locationRepository.findById(locationId).orElseThrow();
        }
        return locationRepository.findByWarehouse_IdAndDeletedFalseOrderByCodeAsc(warehouseId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kho chua co vi tri luu tru. Hay tao/chon vi tri truoc khi nhap kho."));
    }

    private StockBalance findOrCreateBalance(Material material, MaterialBatch batch, Warehouse warehouse, StorageLocation location) {
        return balanceRepository.findByMaterial_IdAndBatch_IdAndWarehouse_IdAndLocation_Id(
                material.getId(), batch.getId(), warehouse.getId(), location.getId()).orElseGet(() -> {
            StockBalance balance = new StockBalance();
            balance.setMaterial(material);
            balance.setBatch(batch);
            balance.setWarehouse(warehouse);
            balance.setLocation(location);
            return balance;
        });
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
