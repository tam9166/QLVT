package com.qlvt.entity;

import com.qlvt.enums.StockAdjustmentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_adjustments")
public class StockAdjustment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String adjustmentCode;
    @ManyToOne
    private InventoryCount inventoryCount;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    @Enumerated(EnumType.STRING)
    private StockAdjustmentStatus status = StockAdjustmentStatus.DRAFT;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String managerApprovedBy;
    private LocalDateTime managerApprovedAt;
    private String accountantApprovedBy;
    private LocalDateTime accountantApprovedAt;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String rejectedReason;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "stockAdjustment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockAdjustmentLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdjustmentCode() { return adjustmentCode; }
    public void setAdjustmentCode(String adjustmentCode) { this.adjustmentCode = adjustmentCode; }
    public InventoryCount getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(InventoryCount inventoryCount) { this.inventoryCount = inventoryCount; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public StockAdjustmentStatus getStatus() { return status; }
    public void setStatus(StockAdjustmentStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getManagerApprovedBy() { return managerApprovedBy; }
    public void setManagerApprovedBy(String managerApprovedBy) { this.managerApprovedBy = managerApprovedBy; }
    public LocalDateTime getManagerApprovedAt() { return managerApprovedAt; }
    public void setManagerApprovedAt(LocalDateTime managerApprovedAt) { this.managerApprovedAt = managerApprovedAt; }
    public String getAccountantApprovedBy() { return accountantApprovedBy; }
    public void setAccountantApprovedBy(String accountantApprovedBy) { this.accountantApprovedBy = accountantApprovedBy; }
    public LocalDateTime getAccountantApprovedAt() { return accountantApprovedAt; }
    public void setAccountantApprovedAt(LocalDateTime accountantApprovedAt) { this.accountantApprovedAt = accountantApprovedAt; }
    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<StockAdjustmentLine> getLines() { return lines; }
    public void setLines(List<StockAdjustmentLine> lines) { this.lines = lines; }

    public boolean canSubmit() {
        return status == StockAdjustmentStatus.DRAFT;
    }

    public boolean canApproveManager() {
        return status == StockAdjustmentStatus.SUBMITTED;
    }

    public boolean canApproveAccountant() {
        return status == StockAdjustmentStatus.APPROVED_BY_MANAGER;
    }
}
