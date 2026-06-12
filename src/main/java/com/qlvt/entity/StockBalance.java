package com.qlvt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_balances", uniqueConstraints = @UniqueConstraint(name = "uk_stock_balance_scope", columnNames = {"material_id", "batch_id", "warehouse_id", "location_id"}))
public class StockBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    private Material material;

    @ManyToOne(optional = false)
    private MaterialBatch batch;

    @ManyToOne(optional = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false)
    private StorageLocation location;

    private int actualQuantity;
    private int reservedQuantity;
    private int pendingIssueQuantity;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public int getAvailableQuantity() {
        return actualQuantity - reservedQuantity - pendingIssueQuantity;
    }

    public void validate() {
        if (actualQuantity < 0 || reservedQuantity < 0 || pendingIssueQuantity < 0) {
            throw new IllegalStateException("Tồn kho không được âm");
        }
        if (reservedQuantity + pendingIssueQuantity > actualQuantity) {
            throw new IllegalStateException("Số lượng giữ/chờ xuất vượt tồn thực tế");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public int getPendingIssueQuantity() { return pendingIssueQuantity; }
    public void setPendingIssueQuantity(int pendingIssueQuantity) { this.pendingIssueQuantity = pendingIssueQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
