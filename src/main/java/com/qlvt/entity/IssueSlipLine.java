package com.qlvt.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_slip_lines")
public class IssueSlipLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private IssueSlip issueSlip;
    @ManyToOne(optional = false)
    private Material material;
    private int requestedQuantity;
    private int approvedQuantity;
    private int issuedQuantity;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;
    @OneToMany(mappedBy = "issueSlipLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssueBatchAllocation> allocations = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IssueSlip getIssueSlip() { return issueSlip; }
    public void setIssueSlip(IssueSlip issueSlip) { this.issueSlip = issueSlip; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public int getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(int approvedQuantity) { this.approvedQuantity = approvedQuantity; }
    public int getIssuedQuantity() { return issuedQuantity; }
    public void setIssuedQuantity(int issuedQuantity) { this.issuedQuantity = issuedQuantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<IssueBatchAllocation> getAllocations() { return allocations; }
    public void setAllocations(List<IssueBatchAllocation> allocations) { this.allocations = allocations; }
}
