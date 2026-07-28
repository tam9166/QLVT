package com.qlvt.entity;

import com.qlvt.enums.RequestStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "material_requests", indexes = @Index(name = "idx_requests_status", columnList = "status"))
public class MaterialRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(columnDefinition = "nvarchar(160)")
    private String department;
    @Column(columnDefinition = "nvarchar(120)")
    private String requester;
    @Column(columnDefinition = "nvarchar(80)")
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequestStatus status = RequestStatus.SUBMITTED;

    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime submittedAt;
    @Column(columnDefinition = "nvarchar(120)")
    private String departmentApprovedBy;
    private LocalDateTime departmentApprovedAt;
    @Column(columnDefinition = "nvarchar(120)")
    private String warehouseApprovedBy;
    private LocalDateTime warehouseApprovedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String rejectedReason;
    @Column(columnDefinition = "nvarchar(120)")
    private String receivedBy;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialRequestLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRequester() { return requester; }
    public void setRequester(String requester) { this.requester = requester; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getDepartmentApprovedBy() { return departmentApprovedBy; }
    public void setDepartmentApprovedBy(String departmentApprovedBy) { this.departmentApprovedBy = departmentApprovedBy; }
    public LocalDateTime getDepartmentApprovedAt() { return departmentApprovedAt; }
    public void setDepartmentApprovedAt(LocalDateTime departmentApprovedAt) { this.departmentApprovedAt = departmentApprovedAt; }
    public String getWarehouseApprovedBy() { return warehouseApprovedBy; }
    public void setWarehouseApprovedBy(String warehouseApprovedBy) { this.warehouseApprovedBy = warehouseApprovedBy; }
    public LocalDateTime getWarehouseApprovedAt() { return warehouseApprovedAt; }
    public void setWarehouseApprovedAt(LocalDateTime warehouseApprovedAt) { this.warehouseApprovedAt = warehouseApprovedAt; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<MaterialRequestLine> getLines() { return lines; }
    public void setLines(List<MaterialRequestLine> lines) { this.lines = lines; }

    public boolean canApproveDepartment() {
        return status == RequestStatus.DRAFT || status == RequestStatus.SUBMITTED;
    }

    public boolean canRejectDepartment() {
        return canApproveDepartment();
    }

    public boolean canReserveStock() {
        return status == RequestStatus.DEPARTMENT_APPROVED || status == RequestStatus.WAREHOUSE_APPROVED;
    }

    public boolean canRejectWarehouse() {
        return status == RequestStatus.DEPARTMENT_APPROVED
                || status == RequestStatus.WAREHOUSE_APPROVED;
    }

    public boolean canCreateIssueSlip() {
        return status == RequestStatus.RESERVED || status == RequestStatus.PARTIALLY_APPROVED;
    }

    public boolean canCancel() {
        return status == RequestStatus.DRAFT
                || status == RequestStatus.SUBMITTED
                || status == RequestStatus.DEPARTMENT_APPROVED
                || status == RequestStatus.WAREHOUSE_APPROVED
                || status == RequestStatus.PARTIALLY_APPROVED
                || status == RequestStatus.RESERVED;
    }
}
