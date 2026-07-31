package com.qlvt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_locations", indexes = @Index(name = "idx_locations_warehouse_code", columnList = "warehouse_id,code", unique = true))
public class StorageLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Warehouse warehouse;

    @ManyToOne
    private StorageLocation parent;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(nullable = false, columnDefinition = "nvarchar(150)")
    private String name;

    private String locationType;
    @Column(columnDefinition = "nvarchar(500)")
    private String description;
    private boolean active = true;
    private Boolean deleted = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public StorageLocation getParent() { return parent; }
    public void setParent(StorageLocation parent) { this.parent = parent; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDeleted() { return Boolean.TRUE.equals(deleted); }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
