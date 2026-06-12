package com.qlvt.controller;

import com.qlvt.repository.AuditLogRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StockMovementRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {
    private final MaterialRepository materialRepository;
    private final StockMovementRepository movementRepository;
    private final AuditLogRepository auditLogRepository;

    public ReportController(MaterialRepository materialRepository, StockMovementRepository movementRepository, AuditLogRepository auditLogRepository) {
        this.materialRepository = materialRepository;
        this.movementRepository = movementRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String reports(Model model) {
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("movements", movementRepository.findTop20ByOrderByCreatedAtDesc());
        model.addAttribute("auditLogs", auditLogRepository.findTop30ByOrderByCreatedAtDesc());
        return "reports/index";
    }

    @GetMapping("/stock.csv")
    public ResponseEntity<String> exportStockCsv() {
        StringBuilder csv = new StringBuilder("Ma vat tu,Ten vat tu,Ton thuc te,Ton kha dung,Gia uoc tinh\n");
        materialRepository.findByDeletedFalseOrderByCodeAsc().forEach(m -> csv.append(escape(m.getCode())).append(',')
                .append(escape(m.getName())).append(',')
                .append(m.getActualQuantity()).append(',')
                .append(m.getAvailableQuantity()).append(',')
                .append(m.getEstimatedUnitPrice() == null ? "" : m.getEstimatedUnitPrice()).append('\n'));
        return csv("bao-cao-ton-kho.csv", csv.toString());
    }

    @GetMapping("/movements.csv")
    public ResponseEntity<String> exportMovementsCsv() {
        StringBuilder csv = new StringBuilder("Thoi gian,Loai,Vat tu,Lo,Kho,So luong,Ton truoc,Ton sau,Tham chieu\n");
        movementRepository.findAll().forEach(m -> csv.append(m.getCreatedAt()).append(',')
                .append(m.getMovementType()).append(',')
                .append(escape(m.getMaterial().getCode() + " - " + m.getMaterial().getName())).append(',')
                .append(escape(m.getBatch() == null ? "" : m.getBatch().getBatchNumber())).append(',')
                .append(escape(m.getWarehouse() == null ? "" : m.getWarehouse().getName())).append(',')
                .append(m.getQuantity()).append(',')
                .append(m.getBeforeQuantity()).append(',')
                .append(m.getAfterQuantity()).append(',')
                .append(escape(m.getReferenceType() + " " + m.getReferenceCode())).append('\n'));
        return csv("bao-cao-nhap-xuat-ton.csv", csv.toString());
    }

    private ResponseEntity<String> csv(String fileName, String content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(content);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
