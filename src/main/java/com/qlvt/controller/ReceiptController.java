package com.qlvt.controller;

import com.qlvt.repository.*;
import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.service.AttachmentService;
import com.qlvt.service.InvoicePdfService;
import com.qlvt.service.VoucherDeletionService;
import com.qlvt.service.WarehouseWorkflowService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/receipts")
public class ReceiptController {
    private final ReceiptRepository receiptRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseWorkflowService workflowService;
    private final AttachmentService attachmentService;
    private final InvoicePdfService invoicePdfService;
    private final VoucherDeletionService voucherDeletionService;

    public ReceiptController(ReceiptRepository receiptRepository, MaterialRepository materialRepository,
                             WarehouseRepository warehouseRepository, StorageLocationRepository locationRepository,
                             SupplierRepository supplierRepository, WarehouseWorkflowService workflowService,
                             AttachmentService attachmentService,
                             InvoicePdfService invoicePdfService,
                             VoucherDeletionService voucherDeletionService) {
        this.receiptRepository = receiptRepository;
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.supplierRepository = supplierRepository;
        this.workflowService = workflowService;
        this.attachmentService = attachmentService;
        this.invoicePdfService = invoicePdfService;
        this.voucherDeletionService = voucherDeletionService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("receipts", receiptRepository.findTop30ByOrderByCreatedAtDesc());
        return "receipts/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        shared(model);
        return "receipts/form";
    }

    @PostMapping
    public String create(@RequestParam List<Long> materialId,
                         @RequestParam Long warehouseId,
                         @RequestParam List<Long> locationId,
                         @RequestParam(required = false) Long supplierId,
                         @RequestParam(required = false) List<String> manufactureDate,
                         @RequestParam(required = false) List<String> expiryDate,
                         @RequestParam List<Integer> quantity,
                         @RequestParam(required = false) List<BigDecimal> unitPrice,
                         @RequestParam(required = false) String note,
                         Authentication authentication) {
        Long lastReceiptId = null;
        for (int i = 0; i < materialId.size(); i++) {
            var receipt = workflowService.createReceipt(materialId.get(i), warehouseId, locationId.get(i), supplierId,
                    parseDate(manufactureDate, i), parseDate(expiryDate, i), quantity.get(i), valueAt(unitPrice, i), note, authentication.getName());
            lastReceiptId = receipt.getId();
        }
        return materialId.size() == 1 && lastReceiptId != null ? "redirect:/receipts/" + lastReceiptId : "redirect:/receipts";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {
        var receipt = receiptRepository.findById(id).orElseThrow();
        model.addAttribute("receipt", receipt);
        model.addAttribute("lines", receipt.getLines().stream().toList());
        model.addAttribute("attachments", attachmentService.list(AttachmentReferenceType.RECEIPT, id));
        model.addAttribute("attachmentType", AttachmentReferenceType.RECEIPT);
        return "receipts/detail";
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String edit(@PathVariable Long id, Model model) {
        var receipt = receiptRepository.findById(id).orElseThrow();
        model.addAttribute("receipt", receipt);
        model.addAttribute("line", receipt.getLines().isEmpty() ? null : receipt.getLines().get(0));
        shared(model);
        return "receipts/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam Long materialId,
                         @RequestParam Long warehouseId,
                         @RequestParam Long locationId,
                         @RequestParam(required = false) Long supplierId,
                         @RequestParam(required = false) String manufactureDate,
                         @RequestParam(required = false) String expiryDate,
                         @RequestParam int quantity,
                         @RequestParam(required = false) BigDecimal unitPrice,
                         @RequestParam(required = false) String note,
                         Authentication authentication) {
        workflowService.updateReceiptDraft(id, materialId, warehouseId, locationId, supplierId,
                parseDate(manufactureDate), parseDate(expiryDate), quantity, unitPrice, note, authentication.getName());
        return "redirect:/receipts/" + id;
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, Authentication authentication) {
        workflowService.confirmReceipt(id, authentication.getName());
        return "redirect:/receipts/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam String reason,
                         Authentication authentication) {
        workflowService.cancelReceipt(id, reason, authentication.getName());
        return "redirect:/receipts/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, Authentication authentication) {
        voucherDeletionService.deleteReceipt(id, authentication.getName());
        return "redirect:/receipts";
    }

    @GetMapping("/{id}/invoice.pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> invoice(@PathVariable Long id) {
        var receipt = receiptRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + receipt.getReceiptCode() + ".pdf\"")
                .body(invoicePdfService.receiptInvoice(receipt));
    }

    private void shared(Model model) {
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("locations", locationRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
    }

    private LocalDate parseDate(List<String> values, int index) {
        return values == null || values.size() <= index ? null : parseDate(values.get(index));
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private BigDecimal valueAt(List<BigDecimal> values, int index) {
        return values == null || values.size() <= index ? null : values.get(index);
    }
}
