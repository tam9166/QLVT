package com.qlvt.service;

import com.qlvt.entity.AppUser;
import com.qlvt.entity.Attachment;
import com.qlvt.enums.AttachmentReferenceType;
import com.qlvt.enums.UserRole;
import com.qlvt.repository.AttachmentRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             FileStorageService fileStorageService,
                             CurrentUserService currentUserService,
                             AuditService auditService) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    public List<Attachment> list(AttachmentReferenceType referenceType, Long referenceId) {
        checkCanAccess(referenceType);
        return attachmentRepository.findByReferenceTypeAndReferenceIdAndDeletedFalseOrderByUploadedAtDesc(referenceType, referenceId);
    }

    @Transactional
    public Attachment upload(AttachmentReferenceType referenceType, Long referenceId, MultipartFile file, String note, String username) {
        checkCanAccess(referenceType);
        FileStorageService.StoredFile storedFile = fileStorageService.store(file);
        Attachment attachment = new Attachment();
        attachment.setReferenceType(referenceType);
        attachment.setReferenceId(referenceId);
        attachment.setOriginalFileName(storedFile.originalFileName());
        attachment.setStoredFileName(storedFile.storedFileName());
        attachment.setStoragePath(storedFile.storagePath());
        attachment.setContentType(storedFile.contentType());
        attachment.setFileSize(storedFile.fileSize());
        attachment.setNote(note);
        attachment.setUploadedBy(username);
        attachmentRepository.save(attachment);
        auditService.log(username, "UPLOAD_ATTACHMENT", referenceType.name(), referenceId.toString(), "Tải lên file đính kèm");
        return attachment;
    }

    public Resource download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow();
        if (attachment.isDeleted()) {
            throw new IllegalStateException("File đính kèm đã bị xóa");
        }
        checkCanAccess(attachment.getReferenceType());
        return fileStorageService.load(attachment.getStoragePath());
    }

    public Attachment getForDownload(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow();
        checkCanAccess(attachment.getReferenceType());
        return attachment;
    }

    @Transactional
    public void softDelete(Long attachmentId, String username) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElseThrow();
        checkCanAccess(attachment.getReferenceType());
        attachment.setDeleted(true);
        attachmentRepository.save(attachment);
        auditService.log(username, "DELETE_ATTACHMENT", attachment.getReferenceType().name(), attachment.getReferenceId().toString(), "Xóa mềm file đính kèm");
    }

    private void checkCanAccess(AttachmentReferenceType referenceType) {
        AppUser user = currentUserService.currentUser();
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER) {
            return;
        }
        boolean allowed = switch (referenceType) {
            case RECEIPT, PURCHASE_ORDER, SUPPLIER -> user.getRole() == UserRole.WAREHOUSE_STAFF || user.getRole() == UserRole.ACCOUNTANT || user.getRole() == UserRole.PROCUREMENT;
            case MATERIAL_REQUEST, ISSUE_SLIP, DEPARTMENT_RETURN -> user.getRole() == UserRole.WAREHOUSE_STAFF || user.getRole() == UserRole.DEPARTMENT_HEAD || user.getRole() == UserRole.DEPARTMENT_STAFF;
            case STOCK_ADJUSTMENT, DESTRUCTION_SLIP, RECALL_ORDER, MATERIAL -> user.getRole() == UserRole.WAREHOUSE_STAFF || user.getRole() == UserRole.ACCOUNTANT;
        };
        if (!allowed) {
            throw new AccessDeniedException("Bạn không có quyền truy cập file đính kèm này");
        }
    }
}
