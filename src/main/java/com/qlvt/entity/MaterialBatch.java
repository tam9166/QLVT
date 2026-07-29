package com.qlvt.entity;

import com.qlvt.enums.BatchStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_batches", indexes = {
        @Index(name = "idx_batches_number", columnList = "batchNumber"),
        @Index(name = "idx_batches_expiry", columnList = "expiryDate")
})
public class MaterialBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    private Material material;

    @ManyToOne(optional = false)
    private Warehouse warehouse;

    @ManyToOne
    private StorageLocation location;

    @ManyToOne
    private Supplier supplier;

    @Column(nullable = false, length = 80)
    private String batchNumber;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private LocalDate receiptDate;
    private Integer initialQuantity = 0;
    private int quantity;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchStatus status = BatchStatus.AVAILABLE;

    public boolean canIssue(LocalDate today) {
        return status == BatchStatus.AVAILABLE && quantity > 0 && (expiryDate == null || expiryDate.isAfter(today));
    }

    public boolean canQuarantine(LocalDate today) {
        return status == BatchStatus.AVAILABLE
                && (expiryDate == null || expiryDate.isAfter(today));
    }

    public boolean canReleaseFromQuarantine(LocalDate today) {
        return status == BatchStatus.QUARANTINED
                && (expiryDate == null || expiryDate.isAfter(today));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getInitialQuantity() { return initialQuantity == null ? 0 : initialQuantity; }
    public void setInitialQuantity(int initialQuantity) { this.initialQuantity = initialQuantity; }
    public int getCurrentQuantity() { return quantity; }
    public void setCurrentQuantity(int currentQuantity) { this.quantity = currentQuantity; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
