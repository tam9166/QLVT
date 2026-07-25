package com.qlvt.entity;

import com.qlvt.enums.InventoryCountStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_counts")
public class InventoryCount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String countCode;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @Enumerated(EnumType.STRING)
    private InventoryCountStatus status = InventoryCountStatus.DRAFT;
    private String startedBy;
    private LocalDateTime startedAt;
    private String completedBy;
    private LocalDateTime completedAt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "inventoryCount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryCountLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCountCode() { return countCode; }
    public void setCountCode(String countCode) { this.countCode = countCode; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public InventoryCountStatus getStatus() { return status; }
    public void setStatus(InventoryCountStatus status) { this.status = status; }
    public String getStartedBy() { return startedBy; }
    public void setStartedBy(String startedBy) { this.startedBy = startedBy; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<InventoryCountLine> getLines() { return lines; }
    public void setLines(List<InventoryCountLine> lines) { this.lines = lines; }

    public boolean canEditLines() {
        return status == InventoryCountStatus.DRAFT || status == InventoryCountStatus.COUNTING;
    }

    public boolean canComplete() {
        return canEditLines();
    }

    public boolean canCancel() {
        return canEditLines();
    }

    public boolean canCreateAdjustment() {
        return status == InventoryCountStatus.COMPLETED;
    }
}
