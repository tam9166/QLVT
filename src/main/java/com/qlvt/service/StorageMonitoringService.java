package com.qlvt.service;

import com.qlvt.entity.Material;
import com.qlvt.entity.StorageCondition;
import com.qlvt.entity.TemperatureLog;
import com.qlvt.entity.Warehouse;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StorageConditionRepository;
import com.qlvt.repository.TemperatureLogRepository;
import com.qlvt.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StorageMonitoringService {
    private final StorageConditionRepository conditionRepository;
    private final TemperatureLogRepository temperatureLogRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditService auditService;

    public StorageMonitoringService(StorageConditionRepository conditionRepository,
                                    TemperatureLogRepository temperatureLogRepository,
                                    MaterialRepository materialRepository,
                                    WarehouseRepository warehouseRepository,
                                    AuditService auditService) {
        this.conditionRepository = conditionRepository;
        this.temperatureLogRepository = temperatureLogRepository;
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public StorageCondition saveCondition(Long materialId,
                                          BigDecimal minTemperature,
                                          BigDecimal maxTemperature,
                                          BigDecimal minHumidity,
                                          BigDecimal maxHumidity,
                                          boolean lightSensitive,
                                          boolean coldChainRequired,
                                          String note,
                                          String username) {
        validateRange(minTemperature, maxTemperature, "Nhiệt độ");
        validateRange(minHumidity, maxHumidity, "Độ ẩm");
        Material material = materialRepository.findById(materialId).orElseThrow();
        StorageCondition condition = conditionRepository.findByMaterial_Id(materialId).orElseGet(StorageCondition::new);
        condition.setMaterial(material);
        condition.setMinTemperature(minTemperature);
        condition.setMaxTemperature(maxTemperature);
        condition.setMinHumidity(minHumidity);
        condition.setMaxHumidity(maxHumidity);
        condition.setLightSensitive(lightSensitive);
        condition.setColdChainRequired(coldChainRequired);
        condition.setNote(note);
        condition.setUpdatedAt(LocalDateTime.now());
        conditionRepository.save(condition);

        String summary = buildConditionSummary(condition);
        material.setStorageCondition(summary);
        material.setUpdatedAt(LocalDateTime.now());
        materialRepository.save(material);
        auditService.log(username, "UPSERT_STORAGE_CONDITION", "MATERIAL", material.getCode(), summary);
        return condition;
    }

    @Transactional
    public TemperatureLog recordTemperature(Long warehouseId,
                                            BigDecimal temperature,
                                            BigDecimal humidity,
                                            String note,
                                            String username) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();
        TemperatureLog log = new TemperatureLog();
        log.setWarehouse(warehouse);
        log.setRecordedAt(LocalDateTime.now());
        log.setTemperature(temperature);
        log.setHumidity(humidity);
        log.setRecordedBy(username);
        log.setNote(note);
        log.setStatus(evaluateStatus(warehouse, temperature, humidity));
        temperatureLogRepository.save(log);
        auditService.log(username, "RECORD_TEMPERATURE", "WAREHOUSE", warehouse.getCode(),
                "Nhiệt độ " + valueOrDash(temperature) + " C, độ ẩm " + valueOrDash(humidity) + "%, trạng thái " + log.getStatus());
        return log;
    }

    @Transactional(readOnly = true)
    public List<TemperatureLog> riskyLogs() {
        return temperatureLogRepository.findTop10ByStatusInOrderByRecordedAtDesc(List.of("WARNING", "RISK"));
    }

    @Transactional(readOnly = true)
    public long recentRiskCount() {
        return temperatureLogRepository.countByStatusInAndRecordedAtAfter(List.of("WARNING", "RISK"), LocalDateTime.now().minusDays(7));
    }

    private String evaluateStatus(Warehouse warehouse, BigDecimal temperature, BigDecimal humidity) {
        if (temperature == null && humidity == null) {
            return "NORMAL";
        }
        if (isOutside(temperature, BigDecimal.valueOf(2), BigDecimal.valueOf(30))
                || isOutside(humidity, BigDecimal.ZERO, BigDecimal.valueOf(75))) {
            return "RISK";
        }
        boolean coldWarehouse = isColdWarehouse(warehouse);
        for (StorageCondition condition : conditionRepository.findByColdChainRequiredTrueOrderByMaterial_CodeAsc()) {
            if (coldWarehouse && (isOutside(temperature, condition.getMinTemperature(), condition.getMaxTemperature())
                    || isOutside(humidity, condition.getMinHumidity(), condition.getMaxHumidity()))) {
                return "RISK";
            }
        }
        if (isOutside(temperature, BigDecimal.valueOf(4), BigDecimal.valueOf(25))
                || isOutside(humidity, BigDecimal.ZERO, BigDecimal.valueOf(65))) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private boolean isColdWarehouse(Warehouse warehouse) {
        String text = ((warehouse.getType() == null ? "" : warehouse.getType()) + " " + warehouse.getName()).toLowerCase();
        return text.contains("cold") || text.contains("lạnh") || text.contains("lanh");
    }

    private boolean isOutside(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return false;
        }
        if (min != null && value.compareTo(min) < 0) {
            return true;
        }
        return max != null && value.compareTo(max) > 0;
    }

    private void validateRange(BigDecimal min, BigDecimal max, String label) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException(label + " tối thiểu không được lớn hơn tối đa");
        }
    }

    private String buildConditionSummary(StorageCondition condition) {
        StringBuilder builder = new StringBuilder();
        if (condition.getMinTemperature() != null || condition.getMaxTemperature() != null) {
            builder.append("Nhiệt độ ")
                    .append(valueOrDash(condition.getMinTemperature()))
                    .append("-")
                    .append(valueOrDash(condition.getMaxTemperature()))
                    .append(" C. ");
        }
        if (condition.getMinHumidity() != null || condition.getMaxHumidity() != null) {
            builder.append("Độ ẩm ")
                    .append(valueOrDash(condition.getMinHumidity()))
                    .append("-")
                    .append(valueOrDash(condition.getMaxHumidity()))
                    .append("%. ");
        }
        if (condition.isColdChainRequired()) {
            builder.append("Yêu cầu chuỗi lạnh. ");
        }
        if (condition.isLightSensitive()) {
            builder.append("Tránh ánh sáng trực tiếp. ");
        }
        if (condition.getNote() != null && !condition.getNote().isBlank()) {
            builder.append(condition.getNote().trim());
        }
        return builder.isEmpty() ? "Bảo quản theo hướng dẫn nhà sản xuất" : builder.toString().trim();
    }

    private String valueOrDash(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }
}
