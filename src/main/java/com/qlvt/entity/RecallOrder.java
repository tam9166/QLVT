package com.qlvt.entity;

import com.qlvt.enums.RecallStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recall_orders")
public class RecallOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String recallCode;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private Material material;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    @Enumerated(EnumType.STRING)
    private RecallStatus status = RecallStatus.DRAFT;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    @OneToMany(mappedBy = "recallOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecallOrderLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecallCode() { return recallCode; }
    public void setRecallCode(String recallCode) { this.recallCode = recallCode; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public RecallStatus getStatus() { return status; }
    public void setStatus(RecallStatus status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<RecallOrderLine> getLines() { return lines; }
    public void setLines(List<RecallOrderLine> lines) { this.lines = lines; }

    public boolean canActivate() {
        return status == RecallStatus.DRAFT;
    }

    public boolean canCancel() {
        return status == RecallStatus.DRAFT;
    }

    public boolean canRespond() {
        return status == RecallStatus.ACTIVE;
    }

    public boolean hasPendingResponses() {
        return lines.stream().anyMatch(line -> !"RESPONDED".equals(line.getStatus()));
    }

    public boolean canComplete() {
        return status == RecallStatus.ACTIVE && !hasPendingResponses();
    }
}
