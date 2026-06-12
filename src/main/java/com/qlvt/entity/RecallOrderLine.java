package com.qlvt.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recall_order_lines")
public class RecallOrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private RecallOrder recallOrder;
    private String department;
    private int issuedQuantity;
    private int remainingQuantity;
    private int returnedQuantity;
    private String status = "PENDING";
    @Column(columnDefinition = "nvarchar(500)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RecallOrder getRecallOrder() { return recallOrder; }
    public void setRecallOrder(RecallOrder recallOrder) { this.recallOrder = recallOrder; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getIssuedQuantity() { return issuedQuantity; }
    public void setIssuedQuantity(int issuedQuantity) { this.issuedQuantity = issuedQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
