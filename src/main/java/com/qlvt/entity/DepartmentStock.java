package com.qlvt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_stocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_department_stock", columnNames = {"department", "material_id", "batch_id"}))
public class DepartmentStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(nullable = false, columnDefinition = "nvarchar(120)")
    private String department;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    private int quantityOnHand;
    private int quantityUsed;
    private int quantityDamaged;
    private int quantityLost;
    private int quantityReturned;
    private LocalDateTime lastReceivedAt;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void validate() {
        if (quantityOnHand < 0) {
            throw new IllegalStateException("Tồn tại khoa không được âm");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public int getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(int quantityUsed) { this.quantityUsed = quantityUsed; }
    public int getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(int quantityDamaged) { this.quantityDamaged = quantityDamaged; }
    public int getQuantityLost() { return quantityLost; }
    public void setQuantityLost(int quantityLost) { this.quantityLost = quantityLost; }
    public int getQuantityReturned() { return quantityReturned; }
    public void setQuantityReturned(int quantityReturned) { this.quantityReturned = quantityReturned; }
    public LocalDateTime getLastReceivedAt() { return lastReceivedAt; }
    public void setLastReceivedAt(LocalDateTime lastReceivedAt) { this.lastReceivedAt = lastReceivedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
