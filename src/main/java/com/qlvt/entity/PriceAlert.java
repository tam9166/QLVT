package com.qlvt.entity;

import com.qlvt.enums.PriceAlertLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne
    private Supplier supplier;
    @Column(precision = 18, scale = 2)
    private BigDecimal oldPrice = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    private BigDecimal newPrice = BigDecimal.ZERO;
    @Column(precision = 8, scale = 2)
    private BigDecimal differencePercent = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PriceAlertLevel alertLevel = PriceAlertLevel.NORMAL;
    @ManyToOne
    private Receipt receipt;
    @Column(columnDefinition = "nvarchar(1000)")
    private String message;
    private boolean resolved;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
    public BigDecimal getDifferencePercent() { return differencePercent; }
    public void setDifferencePercent(BigDecimal differencePercent) { this.differencePercent = differencePercent; }
    public PriceAlertLevel getAlertLevel() { return alertLevel; }
    public void setAlertLevel(PriceAlertLevel alertLevel) { this.alertLevel = alertLevel; }
    public Receipt getReceipt() { return receipt; }
    public void setReceipt(Receipt receipt) { this.receipt = receipt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
