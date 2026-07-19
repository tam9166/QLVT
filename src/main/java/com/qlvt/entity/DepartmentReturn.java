package com.qlvt.entity;

import com.qlvt.enums.DepartmentReturnStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department_returns")
public class DepartmentReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String returnCode;
    @Column(nullable = false, columnDefinition = "nvarchar(120)")
    private String department;
    @ManyToOne(optional = false)
    private Warehouse warehouse;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DepartmentReturnStatus status = DepartmentReturnStatus.DRAFT;
    @Column(columnDefinition = "nvarchar(1000)")
    private String reason;
    private String createdBy;
    private String receivedBy;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "departmentReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentReturnLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public DepartmentReturnStatus getStatus() { return status; }
    public void setStatus(DepartmentReturnStatus status) { this.status = status; }
    public boolean canReceiveByWarehouse() { return status == DepartmentReturnStatus.SUBMITTED; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DepartmentReturnLine> getLines() { return lines; }
    public void setLines(List<DepartmentReturnLine> lines) { this.lines = lines; }
}
