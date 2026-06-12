package com.qlvt.service;

import com.qlvt.entity.AuditLog;
import com.qlvt.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, String action, String targetType, String targetCode, String detail) {
        AuditLog log = new AuditLog();
        log.setActorUsername(username);
        log.setAction(action);
        log.setEntityName(targetType);
        log.setEntityId(targetCode);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }

    public void logChange(String username, String action, String entityName, String entityId, String oldValue, String newValue, String detail) {
        AuditLog log = new AuditLog();
        log.setActorUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }
}
