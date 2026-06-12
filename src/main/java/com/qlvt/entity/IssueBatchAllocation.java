package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "issue_batch_allocations")
public class IssueBatchAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private IssueSlipLine issueSlipLine;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @ManyToOne(optional = false)
    private StorageLocation location;
    private int quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IssueSlipLine getIssueSlipLine() { return issueSlipLine; }
    public void setIssueSlipLine(IssueSlipLine issueSlipLine) { this.issueSlipLine = issueSlipLine; }
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
}
