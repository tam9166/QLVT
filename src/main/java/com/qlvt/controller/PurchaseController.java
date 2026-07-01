package com.qlvt.controller;

import com.qlvt.repository.*;
import com.qlvt.service.Prompt3WorkflowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {
    private final PurchaseRequestRepository requestRepository;
    private final PurchaseOrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final Prompt3WorkflowService workflowService;

    public PurchaseController(PurchaseRequestRepository requestRepository, PurchaseOrderRepository orderRepository,
                              SupplierRepository supplierRepository, WarehouseRepository warehouseRepository,
                              StorageLocationRepository locationRepository, Prompt3WorkflowService workflowService) {
        this.requestRepository = requestRepository;
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.workflowService = workflowService;
    }

    @GetMapping
    public String index() { return "redirect:/purchases/requests"; }

    @GetMapping("/requests")
    public String requests(Model model) { model.addAttribute("items", requestRepository.findTop30ByOrderByCreatedAtDesc()); return "purchases/requests"; }
    @PostMapping("/requests/from-low-stock")
    public String createFromLowStock(Authentication authentication) {
        var request = workflowService.createPurchaseRequestFromLowStock(authentication.getName());
        return "redirect:/purchases/requests/" + request.getId();
    }
    @GetMapping("/requests/{id}")
    public String requestDetail(@PathVariable Long id, Model model) {
        model.addAttribute("item", requestRepository.findWithLinesById(id).orElseThrow());
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "purchases/request-detail";
    }
    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id, Authentication authentication) { workflowService.approvePurchaseRequest(id, authentication.getName()); return "redirect:/purchases/requests/" + id; }
    @PostMapping("/requests/{id}/order")
    public String createOrder(@PathVariable Long id, @RequestParam Long supplierId, @RequestParam(required = false) LocalDate expectedDeliveryDate, Authentication authentication) {
        var order = workflowService.createPurchaseOrder(id, supplierId, expectedDeliveryDate, authentication.getName());
        return "redirect:/purchases/orders/" + order.getId();
    }
    @GetMapping("/orders")
    public String orders(Model model) { model.addAttribute("items", orderRepository.findTop30ByOrderByCreatedAtDesc()); return "purchases/orders"; }
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("item", orderRepository.findWithLinesById(id).orElseThrow());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("locations", locationRepository.findByDeletedFalseOrderByCodeAsc());
        return "purchases/order-detail";
    }

    @PostMapping("/orders/{id}/send")
    public String sendOrder(@PathVariable Long id, Authentication authentication) {
        workflowService.sendPurchaseOrder(id, authentication.getName());
        return "redirect:/purchases/orders/" + id;
    }

    @PostMapping("/orders/{id}/receive")
    public String receiveOrder(@PathVariable Long id, @RequestParam Long warehouseId, @RequestParam Long locationId,
                               @RequestParam Map<String, String> parameters, Authentication authentication) {
        var receipt = workflowService.recordPurchaseOrderReceipt(id, warehouseId, locationId, parameters, authentication.getName());
        return "redirect:/receipts/" + receipt.getId();
    }
}
