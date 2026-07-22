package com.qlvt.entity;

import com.qlvt.enums.IssueStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_slips")
public class IssueSlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String issueCode;
    @ManyToOne(optional = false)
    private MaterialRequest materialRequest;
    @Column(columnDefinition = "nvarchar(160)")
    private String department;
    @ManyToOne
    private Warehouse warehouse;
    @Enumerated(EnumType.STRING)
    private IssueStatus status = IssueStatus.DRAFT;
    @Column(columnDefinition = "nvarchar(120)")
    private String createdBy;
    @Column(columnDefinition = "nvarchar(120)")
    private String issuedBy;
    @Column(columnDefinition = "nvarchar(120)")
    private String receivedBy;
    private LocalDateTime issuedAt;
    private LocalDateTime receivedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "issueSlip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssueSlipLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }
    public MaterialRequest getMaterialRequest() { return materialRequest; }
    public void setMaterialRequest(MaterialRequest materialRequest) { this.materialRequest = materialRequest; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<IssueSlipLine> getLines() { return lines; }
    public void setLines(List<IssueSlipLine> lines) { this.lines = lines; }

    public boolean canEdit() {
        return status == IssueStatus.DRAFT || status == IssueStatus.PREPARING;
    }

    public boolean canIssue() {
        return status == IssueStatus.DRAFT || status == IssueStatus.PREPARING;
    }

    public boolean canReceive() {
        return status == IssueStatus.ISSUED;
    }
}
