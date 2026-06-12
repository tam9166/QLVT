package com.qlvt.controller;

import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/{referenceType}/{referenceId}")
    public String upload(@PathVariable AttachmentReferenceType referenceType, @PathVariable Long referenceId,
                         @RequestParam MultipartFile file, @RequestParam(required = false) String note,
                         @RequestParam(required = false) String redirect, Authentication authentication) {
        attachmentService.upload(referenceType, referenceId, file, note, authentication.getName());
        return "redirect:" + (redirect == null || redirect.isBlank() ? "/" : redirect);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        var attachment = attachmentService.getForDownload(id);
        Resource resource = attachmentService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName().replace("\"", "") + "\"")
                .header(HttpHeaders.CONTENT_TYPE, attachment.getContentType())
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String redirect, Authentication authentication) {
        attachmentService.softDelete(id, authentication.getName());
        return "redirect:" + (redirect == null || redirect.isBlank() ? "/" : redirect);
    }
}
