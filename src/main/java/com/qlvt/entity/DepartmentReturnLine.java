package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "department_return_lines")
public class DepartmentReturnLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private DepartmentReturn departmentReturn;
    @ManyToOne(optional = false)
    private DepartmentStock departmentStock;
    @ManyToOne(optional = false)
    private Material material;
    @ManyToOne(optional = false)
    private MaterialBatch batch;
    @ManyToOne(optional = false)
    private StorageLocation location;
    private int quantity;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DepartmentReturn getDepartmentReturn() { return departmentReturn; }
    public void setDepartmentReturn(DepartmentReturn departmentReturn) { this.departmentReturn = departmentReturn; }
    public DepartmentStock getDepartmentStock() { return departmentStock; }
    public void setDepartmentStock(DepartmentStock departmentStock) { this.departmentStock = departmentStock; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public MaterialBatch getBatch() { return batch; }
    public void setBatch(MaterialBatch batch) { this.batch = batch; }
    public StorageLocation getLocation() { return location; }
    public void setLocation(StorageLocation location) { this.location = location; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
