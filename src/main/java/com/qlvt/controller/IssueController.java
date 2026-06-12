package com.qlvt.controller;

import com.qlvt.repository.IssueSlipRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.InvoicePdfService;
import com.qlvt.service.DataPermissionService;
import com.qlvt.service.VoucherDeletionService;
import com.qlvt.service.WarehouseWorkflowService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/issues")
public class IssueController {
    private final IssueSlipRepository issueSlipRepository;
    private final MaterialRepository materialRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final WarehouseWorkflowService workflowService;
    private final InvoicePdfService invoicePdfService;
    private final VoucherDeletionService voucherDeletionService;
    private final DataPermissionService dataPermissionService;

    public IssueController(IssueSlipRepository issueSlipRepository,
                           MaterialRepository materialRepository,
                           WarehouseRepository warehouseRepository,
                           StockBalanceRepository stockBalanceRepository,
                           WarehouseWorkflowService workflowService,
                           InvoicePdfService invoicePdfService,
                           VoucherDeletionService voucherDeletionService,
                           DataPermissionService dataPermissionService) {
        this.issueSlipRepository = issueSlipRepository;
        this.materialRepository = materialRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.workflowService = workflowService;
        this.invoicePdfService = invoicePdfService;
        this.voucherDeletionService = voucherDeletionService;
        this.dataPermissionService = dataPermissionService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("issues", issueSlipRepository.findTop30ByOrderByCreatedAtDesc());
        return "issues/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateCreateForm(model);
        return "issues/form";
    }

    @PostMapping
    public String create(@RequestParam Long warehouseId,
                         @RequestParam Long materialId,
                         @RequestParam int quantity,
                         @RequestParam String department,
                         @RequestParam(required = false) String note,
                         Authentication authentication) {
        var issue = workflowService.createDirectIssueSlip(warehouseId, materialId, quantity, department, note, authentication.getName());
        return "redirect:/issues/" + issue.getId();
    }

    @PostMapping("/from-request/{requestId}")
    public String createFromRequest(@PathVariable Long requestId, Authentication authentication) {
        var issues = workflowService.createIssueSlips(requestId, authentication.getName());
        return issues.size() == 1 ? "redirect:/issues/" + issues.get(0).getId() : "redirect:/issues";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {
        var issue = issueSlipRepository.findById(id).orElseThrow();
        var lines = issue.getLines().stream().toList();
        lines.forEach(line -> line.getAllocations().forEach(allocation -> {
            allocation.getBatch().getBatchNumber();
            if (allocation.getLocation() != null) {
                allocation.getLocation().getCode();
            }
        }));
        model.addAttribute("issue", issue);
        model.addAttribute("lines", lines);
        return "issues/detail";
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String edit(@PathVariable Long id, Model model) {
        var issue = issueSlipRepository.findById(id).orElseThrow();
        model.addAttribute("issue", issue);
        model.addAttribute("line", issue.getLines().isEmpty() ? null : issue.getLines().get(0));
        model.addAttribute("materials", materialRepository.findByDeletedFalseOrderByCodeAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        return "issues/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam Long warehouseId,
                         @RequestParam Long materialId,
                         @RequestParam int quantity,
                         @RequestParam(required = false) String note,
                         Authentication authentication) {
        workflowService.updateIssueDraft(id, warehouseId, materialId, quantity, note, authentication.getName());
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/issue")
    public String issue(@PathVariable Long id, Authentication authentication) {
        workflowService.issue(id, authentication.getName());
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, Authentication authentication) {
        workflowService.receiveIssue(id, authentication.getName());
        return "redirect:/issues/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, Authentication authentication) {
        voucherDeletionService.deleteIssue(id, authentication.getName());
        return "redirect:/issues";
    }

    @GetMapping("/{id}/invoice.pdf")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> invoice(@PathVariable Long id) {
        var issue = issueSlipRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + issue.getIssueCode() + ".pdf\"")
                .body(invoicePdfService.issueInvoice(issue));
    }

    private void populateCreateForm(Model model) {
        var materials = materialRepository.findByDeletedFalseOrderByCodeAsc();
        var warehouses = warehouseRepository.findAll();
        model.addAttribute("materials", materials);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("availableByWarehouseMaterial", availableByWarehouseMaterial(materials, warehouses));
        model.addAttribute("selectedDepartment", defaultDepartment());
    }

    private Map<String, Long> availableByWarehouseMaterial(List<com.qlvt.entity.Material> materials,
                                                           List<com.qlvt.entity.Warehouse> warehouses) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (var warehouse : warehouses) {
            for (var material : materials) {
                result.put(warehouse.getId() + "_" + material.getId(),
                        stockBalanceRepository.sumAvailableByMaterialIdAndWarehouseId(material.getId(), warehouse.getId(), LocalDate.now()));
            }
        }
        return result;
    }

    private String defaultDepartment() {
        var user = dataPermissionService.currentUser();
        return user.getDepartment() == null || user.getDepartment().isBlank()
                ? "Khoa Cấp cứu"
                : user.getDepartment();
    }
}
