package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_count_lines")
public class InventoryCountLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private InventoryCount inventoryCount;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private StorageLocation location;
    private int systemQuantity;
    private Integer actualQuantity;
    private int differenceQuantity;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public void recalculateDifference() {
        differenceQuantity = (actualQuantity == null ? systemQuantity : actualQuantity) - systemQuantity;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InventoryCount getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(InventoryCount inventoryCount) { this.inventoryCount = inventoryCount; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public int getSystemQuantity() { return systemQuantity; }
    public void setSystemQuantity(int systemQuantity) { this.systemQuantity = systemQuantity; }
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; recalculateDifference(); }
    public int getDifferenceQuantity() { return differenceQuantity; }
    public void setDifferenceQuantity(int differenceQuantity) { this.differenceQuantity = differenceQuantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
