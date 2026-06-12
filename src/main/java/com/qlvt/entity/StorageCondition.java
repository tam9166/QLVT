package com.qlvt.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_conditions",
        uniqueConstraints = @UniqueConstraint(name = "uk_storage_condition_material", columnNames = "material_id"))
public class StorageCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Material material;

    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private BigDecimal minHumidity;
    private BigDecimal maxHumidity;
    private boolean lightSensitive;
    private boolean coldChainRequired;

    @Column(columnDefinition = "nvarchar(700)")
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public BigDecimal getMinTemperature() { return minTemperature; }
    public void setMinTemperature(BigDecimal minTemperature) { this.minTemperature = minTemperature; }
    public BigDecimal getMaxTemperature() { return maxTemperature; }
    public void setMaxTemperature(BigDecimal maxTemperature) { this.maxTemperature = maxTemperature; }
    public BigDecimal getMinHumidity() { return minHumidity; }
    public void setMinHumidity(BigDecimal minHumidity) { this.minHumidity = minHumidity; }
    public BigDecimal getMaxHumidity() { return maxHumidity; }
    public void setMaxHumidity(BigDecimal maxHumidity) { this.maxHumidity = maxHumidity; }
    public boolean isLightSensitive() { return lightSensitive; }
    public void setLightSensitive(boolean lightSensitive) { this.lightSensitive = lightSensitive; }
    public boolean isColdChainRequired() { return coldChainRequired; }
    public void setColdChainRequired(boolean coldChainRequired) { this.coldChainRequired = coldChainRequired; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
