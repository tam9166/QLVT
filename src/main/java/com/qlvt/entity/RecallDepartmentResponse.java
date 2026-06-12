package com.qlvt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recall_department_responses")
public class RecallDepartmentResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private RecallOrder recallOrder;
    private String department;
    private String respondedBy;
    private int remainingQuantity;
    private int usedQuantity;
    private int returnedQuantity;
    @Column(columnDefinition = "nvarchar(1000)")
    private String note;
    private LocalDateTime respondedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RecallOrder getRecallOrder() { return recallOrder; }
    public void setRecallOrder(RecallOrder recallOrder) { this.recallOrder = recallOrder; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRespondedBy() { return respondedBy; }
    public void setRespondedBy(String respondedBy) { this.respondedBy = respondedBy; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(int remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public int getUsedQuantity() { return usedQuantity; }
    public void setUsedQuantity(int usedQuantity) { this.usedQuantity = usedQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
