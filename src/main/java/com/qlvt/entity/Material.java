package com.qlvt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_materials_code", columnList = "code", unique = true),
        @Index(name = "idx_materials_name", columnList = "name")
})
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Column(nullable = false, columnDefinition = "nvarchar(220)")
    private String name;

    @Column(columnDefinition = "nvarchar(500)")
    private String aliasText;
    @Column(columnDefinition = "nvarchar(120)")
    private String category;
    @Column(columnDefinition = "nvarchar(60)")
    private String unit;
    @Column(columnDefinition = "nvarchar(160)")
    private String packageSpec;
    @Column(columnDefinition = "nvarchar(300)")
    private String storageCondition;
    private boolean specialControl;
    private String status = "ACTIVE";

    @Min(0)
    private int minStock;

    @Min(0)
    private int maxStock;

    @Min(0)
    private int actualQuantity;

    @Min(0)
    private int reservedQuantity;

    @Min(0)
    private int pendingIssueQuantity;

    private BigDecimal estimatedUnitPrice = BigDecimal.ZERO;
    private boolean deleted;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public int getAvailableQuantity() {
        return Math.max(0, actualQuantity - reservedQuantity - pendingIssueQuantity);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAliasText() { return aliasText; }
    public void setAliasText(String aliasText) { this.aliasText = aliasText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getPackageSpec() { return packageSpec; }
    public void setPackageSpec(String packageSpec) { this.packageSpec = packageSpec; }
    public String getStorageCondition() { return storageCondition; }
    public void setStorageCondition(String storageCondition) { this.storageCondition = storageCondition; }
    public boolean isSpecialControl() { return specialControl; }
    public void setSpecialControl(boolean specialControl) { this.specialControl = specialControl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }
    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }
    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public int getPendingIssueQuantity() { return pendingIssueQuantity; }
    public void setPendingIssueQuantity(int pendingIssueQuantity) { this.pendingIssueQuantity = pendingIssueQuantity; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) { this.estimatedUnitPrice = estimatedUnitPrice; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
