package com.qlvt.entity;

import com.qlvt.enums.DestructionReason;
import jakarta.persistence.*;

@Entity
@Table(name = "destruction_slip_lines")
public class DestructionSlipLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private DestructionSlip destructionSlip;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @ManyToOne(optional = false)
    private StorageLocation location;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private DestructionReason reason = DestructionReason.OTHER;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DestructionSlip getDestructionSlip() { return destructionSlip; }
    public void setDestructionSlip(DestructionSlip destructionSlip) { this.destructionSlip = destructionSlip; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public DestructionReason getReason() { return reason; }
    public void setReason(DestructionReason reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
