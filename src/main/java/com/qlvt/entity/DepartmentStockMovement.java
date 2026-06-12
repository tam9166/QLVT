package com.qlvt.entity;

import com.qlvt.enums.DepartmentStockMovementType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_stock_movements")
public class DepartmentStockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DepartmentStockMovementType movementType;
    @Column(nullable = false, columnDefinition = "nvarchar(120)")
    private String department;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    private int quantity;
    private int beforeQuantity;
    private int afterQuantity;
    private String referenceType;
    private Long referenceId;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DepartmentStockMovementType getMovementType() { return movementType; }
    public void setMovementType(DepartmentStockMovementType movementType) { this.movementType = movementType; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getBeforeQuantity() { return beforeQuantity; }
    public void setBeforeQuantity(int beforeQuantity) { this.beforeQuantity = beforeQuantity; }
    public int getAfterQuantity() { return afterQuantity; }
    public void setAfterQuantity(int afterQuantity) { this.afterQuantity = afterQuantity; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
