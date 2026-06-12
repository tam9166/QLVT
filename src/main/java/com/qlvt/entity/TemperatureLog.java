package com.qlvt.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "temperature_logs", indexes = {
        @Index(name = "idx_temperature_logs_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_temperature_logs_recorded", columnList = "recordedAt")
})
public class TemperatureLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Warehouse warehouse;

    private LocalDateTime recordedAt = LocalDateTime.now();
    private BigDecimal temperature;
    private BigDecimal humidity;

    @Column(columnDefinition = "nvarchar(120)")
    private String recordedBy;

    @Column(length = 30)
    private String status = "NORMAL";

    @Column(columnDefinition = "nvarchar(700)")
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
