package com.qlvt.controller;

import com.qlvt.repository.AuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String actor,
                       @RequestParam(defaultValue = "") String action,
                       @RequestParam(defaultValue = "") String entity,
                       Model model) {
        model.addAttribute("auditLogs", auditLogRepository.search(actor, action, entity));
        model.addAttribute("actor", actor);
        model.addAttribute("action", action);
        model.addAttribute("entity", entity);
        return "admin/audit-logs";
    }
}
