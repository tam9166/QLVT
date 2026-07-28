package com.qlvt.service;

import com.qlvt.entity.MaterialBatch;
import com.qlvt.enums.BatchStatus;
import com.qlvt.exception.ResourceNotFoundException;
import com.qlvt.repository.MaterialBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class BatchWorkflowService {
    private final MaterialBatchRepository batchRepository;
    private final AuditService auditService;

    public BatchWorkflowService(MaterialBatchRepository batchRepository, AuditService auditService) {
        this.batchRepository = batchRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MaterialBatch quarantine(Long id, String reason, String username) {
        MaterialBatch batch = find(id);
        String normalizedReason = requireReason(reason);
        if (!batch.canQuarantine()) {
            throw new IllegalStateException("Chỉ có thể cách ly lô đang ở trạng thái khả dụng.");
        }
        BatchStatus oldStatus = batch.getStatus();
        batch.setStatus(BatchStatus.QUARANTINED);
        batch.setUpdatedAt(LocalDateTime.now());
        auditService.logChange(username, "QUARANTINE_BATCH", "MATERIAL_BATCH", batch.getBatchNumber(),
                oldStatus.name(), BatchStatus.QUARANTINED.name(), normalizedReason);
        return batchRepository.save(batch);
    }

    @Transactional
    public MaterialBatch release(Long id, String reason, String username) {
        MaterialBatch batch = find(id);
        String normalizedReason = requireReason(reason);
        if (!batch.canReleaseFromQuarantine(LocalDate.now())) {
            throw new IllegalStateException("Chỉ có thể gỡ cách ly lô chưa hết hạn đang bị cách ly.");
        }
        batch.setStatus(BatchStatus.AVAILABLE);
        batch.setUpdatedAt(LocalDateTime.now());
        auditService.logChange(username, "RELEASE_BATCH", "MATERIAL_BATCH", batch.getBatchNumber(),
                BatchStatus.QUARANTINED.name(), BatchStatus.AVAILABLE.name(), normalizedReason);
        return batchRepository.save(batch);
    }

    private MaterialBatch find(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lô vật tư."));
    }

    private String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Lý do là bắt buộc.");
        }
        return reason.trim();
    }
}
