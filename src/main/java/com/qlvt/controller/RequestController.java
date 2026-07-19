package com.qlvt.controller;

import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.entity.MaterialRequestLine;
import com.qlvt.entity.RequestApprovalLog;
import com.qlvt.enums.RequestStatus;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.MaterialRequestRepository;
import com.qlvt.repository.RequestApprovalLogRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.service.AuditService;
import com.qlvt.service.DataPermissionService;
import com.qlvt.service.InventoryAlertService;
import com.qlvt.service.WarehouseWorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/requests")
public class RequestController {
    private final MaterialRequestRepository requestRepository;
    private final MaterialRepository materialRepository;
    private final RequestApprovalLogRepository approvalLogRepository;
    private final AuditService auditService;
    private final WarehouseWorkflowService warehouseWorkflowService;
    private final DataPermissionService dataPermissionService;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryAlertService inventoryAlertService;

    public RequestController(MaterialRequestRepository requestRepository, MaterialRepository materialRepository,
                             RequestApprovalLogRepository approvalLogRepository, AuditService auditService,
                             WarehouseWorkflowService warehouseWorkflowService,
                             DataPermissionService dataPermissionService,
                             StockBalanceRepository stockBalanceRepository,
                             InventoryAlertService inventoryAlertService) {
        this.requestRepository = requestRepository;
        this.materialRepository = materialRepository;
        this.approvalLogRepository = approvalLogRepository;
        this.auditService = auditService;
        this.warehouseWorkflowService = warehouseWorkflowService;
        this.dataPermissionService = dataPermissionService;
        this.stockBalanceRepository = stockBalanceRepository;
        this.inventoryAlertService = inventoryAlertService;
    }

    @GetMapping
    public String list(Model model) {
        var user = dataPermissionService.currentUser();
        if (dataPermissionService.canViewAllRequests(user)) {
            model.addAttribute("requests", requestRepository.findTop20ByOrderByCreatedAtDesc());
        } else if (user.getDepartment() != null && !user.getDepartment().isBlank()) {
            model.addAttribute("requests", requestRepository.findTop20ByDepartmentOrderByCreatedAtDesc(user.getDepartment()));
        } else {
            model.addAttribute("requests", requestRepository.findTop20ByRequesterOrderByCreatedAtDesc(user.getUsername()));
        }
        return "requests/list";
    }

    @GetMapping({"/my", "/my-department"})
    public String myRequests(Model model) {
        var user = dataPermissionService.currentUser();
        if (user.getDepartment() != null && !user.getDepartment().isBlank()) {
            model.addAttribute("requests", requestRepository.findTop20ByDepartmentOrderByCreatedAtDesc(user.getDepartment()));
        } else {
            model.addAttribute("requests", requestRepository.findTop20ByRequesterOrderByCreatedAtDesc(user.getUsername()));
        }
        return "requests/list";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {
        dataPermissionService.checkCanViewMaterialRequest(id);
        MaterialRequest request = requestRepository.findById(id).orElseThrow();
        List<MaterialRequestLine> lines = request.getLines().stream().toList();
        model.addAttribute("request", request);
        model.addAttribute("lines", lines);
        model.addAttribute("availableByMaterial", availableByMaterial(lines.stream().map(MaterialRequestLine::getMaterial).toList()));
        model.addAttribute("logs", approvalLogRepository.findByMaterialRequest_IdOrderByCreatedAtAsc(id));
        return "requests/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        populateRequestForm(model, null, null, null, "BINH_THUONG", null, null);
        return "requests/form";
    }

    @PostMapping
    @Transactional
    public String create(@RequestParam String department,
                         @RequestParam List<Long> materialId,
                         @RequestParam List<Integer> quantity,
                         @RequestParam(defaultValue = "BINH_THUONG") String priority,
                          @RequestParam(required = false) String reason,
                          Authentication authentication,
                          Model model) {
        return createMultiple(department, materialId, quantity, priority, reason, authentication, model);
    }

    private String createMultiple(String department, List<Long> materialIds, List<Integer> quantities,
                                  String priority, String reason, Authentication authentication, Model model) {
        if (materialIds.isEmpty() || materialIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một vật tư với số lượng hợp lệ");
        }
        Map<Long, Integer> requestedByMaterial = new LinkedHashMap<>();
        for (int index = 0; index < materialIds.size(); index++) {
            Long selectedMaterialId = materialIds.get(index);
            Integer requestedQuantity = quantities.get(index);
            if (selectedMaterialId == null || requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
            }
            if (requestedByMaterial.putIfAbsent(selectedMaterialId, requestedQuantity) != null) {
                populateRequestForm(model, department, null, null, priority, reason,
                        "Mỗi vật tư chỉ được chọn một lần trong cùng phiếu yêu cầu.");
                return "requests/form";
            }
        }

        Map<Long, Material> materialsById = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> requested : requestedByMaterial.entrySet()) {
            Material material = materialRepository.findById(requested.getKey()).orElseThrow();
            long available = availableQuantity(material.getId());
            if (requested.getValue() > available) {
                if (available == 0) {
                    inventoryAlertService.notifyOutOfStockIfNeeded(material, available, "/alerts#stock-alerts");
                }
                populateRequestForm(model, department, null, null, priority, reason,
                        material.getCode() + " - " + material.getName() + " chỉ còn " + available + " "
                                + (material.getUnit() == null ? "" : material.getUnit())
                                + ", không đủ để lấy " + requested.getValue() + ".");
                return "requests/form";
            }
            materialsById.put(material.getId(), material);
        }

        LocalDateTime now = LocalDateTime.now();
        MaterialRequest request = new MaterialRequest();
        request.setCode(nextCode());
        request.setDepartment(department);
        request.setRequester(authentication.getName());
        request.setPriority(priority);
        request.setNote(reason);
        request.setSubmittedAt(now);
        request.setStatus(RequestStatus.DEPARTMENT_APPROVED);
        request.setDepartmentApprovedBy(authentication.getName());
        request.setDepartmentApprovedAt(now);
        request.setUpdatedAt(now);

        for (Map.Entry<Long, Integer> requested : requestedByMaterial.entrySet()) {
            MaterialRequestLine line = new MaterialRequestLine();
            line.setRequest(request);
            line.setMaterial(materialsById.get(requested.getKey()));
            line.setRequestedQuantity(requested.getValue());
            line.setReason(reason);
            line.setStatus("Chờ kho xử lý");
            request.getLines().add(line);
        }
        requestRepository.save(request);

        RequestApprovalLog log = new RequestApprovalLog();
        log.setMaterialRequest(request);
        log.setAction("NURSE_SUBMITTED");
        log.setActor(authentication.getName());
        log.setNote("Điều dưỡng gửi yêu cầu, bỏ qua bước duyệt trưởng phòng.");
        approvalLogRepository.save(log);
        auditService.log(authentication.getName(), "CREATE_REQUEST", "MATERIAL_REQUEST", request.getCode(), "Tạo yêu cầu cấp vật tư");
        return "redirect:/requests/" + request.getId();
    }

    @PostMapping("/{id}/approve-department")
    public String approveDepartment(@PathVariable Long id, Authentication authentication) {
        dataPermissionService.checkCanApproveDepartmentRequest(id);
        warehouseWorkflowService.approveDepartment(id, authentication.getName());
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/approve-warehouse")
    public String approveWarehouse(@PathVariable Long id, Authentication authentication) {
        dataPermissionService.checkCanProcessWarehouseRequest(id);
        warehouseWorkflowService.reserveForRequest(id, authentication.getName());
        return "redirect:/requests/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                         Authentication authentication) {
        dataPermissionService.checkCanCancelMaterialRequest(id);
        warehouseWorkflowService.cancelRequest(id, authentication.getName(), reason);
        return "redirect:/requests/" + id;
    }

    private String nextCode() {
        String code = "YC-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return requestRepository.existsByCode(code) ? code + "-1" : code;
    }

    private void populateRequestForm(Model model, String department, Long materialId, Integer quantity,
                                     String priority, String reason, String stockError) {
        List<Material> materials = materialRepository.findByDeletedFalseOrderByCodeAsc();
        model.addAttribute("materials", materials);
        model.addAttribute("availableByMaterial", availableByMaterial(materials));
        model.addAttribute("selectedDepartment", department == null ? defaultDepartment() : department);
        model.addAttribute("selectedMaterialId", materialId);
        model.addAttribute("selectedQuantity", quantity);
        model.addAttribute("selectedPriority", priority == null || priority.isBlank() ? "BINH_THUONG" : priority);
        model.addAttribute("selectedReason", reason);
        model.addAttribute("stockError", stockError);
    }

    private Map<Long, Long> availableByMaterial(List<Material> materials) {
        Map<Long, Long> result = new LinkedHashMap<>();
        for (Material material : materials) {
            result.put(material.getId(), availableQuantity(material.getId()));
        }
        return result;
    }

    private long availableQuantity(Long materialId) {
        return stockBalanceRepository.sumAvailableByMaterialId(materialId, LocalDate.now());
    }

    private String defaultDepartment() {
        var user = dataPermissionService.currentUser();
        return user.getDepartment() == null || user.getDepartment().isBlank()
                ? "Khoa Cấp cứu"
                : user.getDepartment();
    }
}
