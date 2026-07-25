package com.qlvt.entity;

import com.qlvt.enums.PurchaseRequestStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String requestCode;
    @Enumerated(EnumType.STRING)
    private PurchaseRequestStatus status = PurchaseRequestStatus.DRAFT;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseRequestLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public PurchaseRequestStatus getStatus() { return status; }
    public void setStatus(PurchaseRequestStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<PurchaseRequestLine> getLines() { return lines; }
    public void setLines(List<PurchaseRequestLine> lines) { this.lines = lines; }

    public boolean canApprove() {
        return status == PurchaseRequestStatus.DRAFT || status == PurchaseRequestStatus.SUBMITTED;
    }

    public boolean canCancel() {
        return status == PurchaseRequestStatus.DRAFT || status == PurchaseRequestStatus.SUBMITTED;
    }

    public boolean canReject() {
        return status == PurchaseRequestStatus.DRAFT || status == PurchaseRequestStatus.SUBMITTED;
    }

    public boolean canCreateOrder() {
        return status == PurchaseRequestStatus.APPROVED;
    }
}
