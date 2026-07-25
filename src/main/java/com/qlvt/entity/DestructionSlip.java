package com.qlvt.entity;

import com.qlvt.enums.DestructionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "destruction_slips")
public class DestructionSlip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String destructionCode;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    @Enumerated(EnumType.STRING)
    private DestructionStatus status = DestructionStatus.DRAFT;
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
    private LocalDateTime destroyedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "destructionSlip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DestructionSlipLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDestructionCode() { return destructionCode; }
    public void setDestructionCode(String destructionCode) { this.destructionCode = destructionCode; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public DestructionStatus getStatus() { return status; }
    public void setStatus(DestructionStatus status) { this.status = status; }
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
    public LocalDateTime getDestroyedAt() { return destroyedAt; }
    public void setDestroyedAt(LocalDateTime destroyedAt) { this.destroyedAt = destroyedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DestructionSlipLine> getLines() { return lines; }
    public void setLines(List<DestructionSlipLine> lines) { this.lines = lines; }

    public boolean canSubmit() {
        return status == DestructionStatus.DRAFT;
    }

    public boolean canApproveManager() {
        return status == DestructionStatus.SUBMITTED;
    }

    public boolean canApproveAccountant() {
        return status == DestructionStatus.APPROVED_BY_MANAGER;
    }

    public boolean canDestroy() {
        return status == DestructionStatus.APPROVED || status == DestructionStatus.APPROVED_BY_ACCOUNTANT;
    }

    public boolean canReject() {
        return status == DestructionStatus.SUBMITTED || status == DestructionStatus.APPROVED_BY_MANAGER;
    }

    public boolean canCancel() {
        return status == DestructionStatus.DRAFT;
    }
}
