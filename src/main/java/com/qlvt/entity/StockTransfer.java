package com.qlvt.entity;

import com.qlvt.enums.StockTransferStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers")
public class StockTransfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String transferCode;
    @ManyToOne(optional = false)
    private Warehouse fromWarehouse;
    @ManyToOne(optional = false)
    private Warehouse toWarehouse;
    @Enumerated(EnumType.STRING)
    private StockTransferStatus status = StockTransferStatus.DRAFT;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String transferredBy;
    private LocalDateTime transferredAt;
    private String receivedBy;
    private LocalDateTime receivedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransferCode() { return transferCode; }
    public void setTransferCode(String transferCode) { this.transferCode = transferCode; }
    public Warehouse getFromWarehouse() { return fromWarehouse; }
    public void setFromWarehouse(Warehouse fromWarehouse) { this.fromWarehouse = fromWarehouse; }
    public Warehouse getToWarehouse() { return toWarehouse; }
    public void setToWarehouse(Warehouse toWarehouse) { this.toWarehouse = toWarehouse; }
    public StockTransferStatus getStatus() { return status; }
    public void setStatus(StockTransferStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getTransferredBy() { return transferredBy; }
    public void setTransferredBy(String transferredBy) { this.transferredBy = transferredBy; }
    public LocalDateTime getTransferredAt() { return transferredAt; }
    public void setTransferredAt(LocalDateTime transferredAt) { this.transferredAt = transferredAt; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<StockTransferLine> getLines() { return lines; }
    public void setLines(List<StockTransferLine> lines) { this.lines = lines; }

    public boolean canSubmit() {
        return status == StockTransferStatus.DRAFT;
    }

    public boolean canApprove() {
        return status == StockTransferStatus.DRAFT || status == StockTransferStatus.SUBMITTED;
    }

    public boolean canReject() {
        return status == StockTransferStatus.SUBMITTED;
    }

    public boolean canExecuteTransfer() {
        return status == StockTransferStatus.APPROVED;
    }

    public boolean canReceive() {
        return status == StockTransferStatus.TRANSFERRED;
    }
}
