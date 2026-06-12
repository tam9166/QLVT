package com.qlvt.controller;

import com.qlvt.service.AdvancedReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports/advanced")
public class AdvancedReportController {
    private final AdvancedReportService reportService;

    public AdvancedReportController(AdvancedReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("report", reportService.load());
        return "reports/advanced";
    }
}
