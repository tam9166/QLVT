package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_adjustment_lines")
public class StockAdjustmentLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private StockAdjustment stockAdjustment;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private StorageLocation location;
    private int systemQuantity;
    private int actualQuantity;
    private int adjustmentQuantity;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StockAdjustment getStockAdjustment() { return stockAdjustment; }
    public void setStockAdjustment(StockAdjustment stockAdjustment) { this.stockAdjustment = stockAdjustment; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public int getSystemQuantity() { return systemQuantity; }
    public void setSystemQuantity(int systemQuantity) { this.systemQuantity = systemQuantity; }
    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
    public int getAdjustmentQuantity() { return adjustmentQuantity; }
    public void setAdjustmentQuantity(int adjustmentQuantity) { this.adjustmentQuantity = adjustmentQuantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isBasedOnCurrentQuantity(int currentQuantity) {
        return systemQuantity == currentQuantity;
    }
}
