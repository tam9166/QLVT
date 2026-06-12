package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_transfer_lines")
public class StockTransferLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private StockTransfer stockTransfer;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private StorageLocation fromLocation;
    @ManyToOne(optional = false)
    private StorageLocation toLocation;
    private int quantity;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StockTransfer getStockTransfer() { return stockTransfer; }
    public void setStockTransfer(StockTransfer stockTransfer) { this.stockTransfer = stockTransfer; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public StorageLocation getFromLocation() { return fromLocation; }
    public void setFromLocation(StorageLocation fromLocation) { this.fromLocation = fromLocation; }
    public StorageLocation getToLocation() { return toLocation; }
    public void setToLocation(StorageLocation toLocation) { this.toLocation = toLocation; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
