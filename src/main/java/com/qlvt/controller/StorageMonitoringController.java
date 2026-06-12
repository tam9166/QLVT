package com.qlvt.controller;

import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StorageConditionRepository;
import com.qlvt.repository.TemperatureLogRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.StorageMonitoringService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/storage-monitoring")
public class StorageMonitoringController {
    private final StorageConditionRepository conditionRepository;
    private final TemperatureLogRepository temperatureLogRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageMonitoringService monitoringService;

    public StorageMonitoringController(StorageConditionRepository conditionRepository,
                                       TemperatureLogRepository temperatureLogRepository,
                                       MaterialRepository materialRepository,
                                       WarehouseRepository warehouseRepository,
                                       StorageMonitoringService monitoringService) {
        this.conditionRepository = conditionRepository;
        this.temperatureLogRepository = temperatureLogRepository;
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.monitoringService = monitoringService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("conditions", conditionRepository.findTop50ByOrderByUpdatedAtDesc());
        model.addAttribute("temperatureLogs", temperatureLogRepository.findTop50ByOrderByRecordedAtDesc());
        model.addAttribute("riskyLogs", monitoringService.riskyLogs());
        return "storage-monitoring/index";
    }

    @PostMapping("/conditions")
    public String saveCondition(@RequestParam Long materialId,
                                @RequestParam(required = false) BigDecimal minTemperature,
                                @RequestParam(required = false) BigDecimal maxTemperature,
                                @RequestParam(required = false) BigDecimal minHumidity,
                                @RequestParam(required = false) BigDecimal maxHumidity,
                                @RequestParam(defaultValue = "false") boolean lightSensitive,
                                @RequestParam(defaultValue = "false") boolean coldChainRequired,
                                @RequestParam(required = false) String note,
                                Authentication authentication) {
        monitoringService.saveCondition(materialId, minTemperature, maxTemperature, minHumidity, maxHumidity,
                lightSensitive, coldChainRequired, note, authentication.getName());
        return "redirect:/storage-monitoring";
    }

    @PostMapping("/temperature-logs")
    public String recordTemperature(@RequestParam Long warehouseId,
                                    @RequestParam(required = false) BigDecimal temperature,
                                    @RequestParam(required = false) BigDecimal humidity,
                                    @RequestParam(required = false) String note,
                                    Authentication authentication) {
        monitoringService.recordTemperature(warehouseId, temperature, humidity, note, authentication.getName());
        return "redirect:/storage-monitoring";
    }
}
