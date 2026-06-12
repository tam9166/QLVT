package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "material_request_lines")
public class MaterialRequestLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private MaterialRequest request;

    @ManyToOne(optional = false)
    private Material material;

    private int requestedQuantity;
    private int approvedQuantity;
    private Integer issuedQuantity = 0;
    private String reason;
    private String status;
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaterialRequest getRequest() { return request; }
    public void setRequest(MaterialRequest request) { this.request = request; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public int getApprovedQuantity() { return approvedQuantity; }
    public void setApprovedQuantity(int approvedQuantity) { this.approvedQuantity = approvedQuantity; }
    public int getIssuedQuantity() { return issuedQuantity == null ? 0 : issuedQuantity; }
    public void setIssuedQuantity(int issuedQuantity) { this.issuedQuantity = issuedQuantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
