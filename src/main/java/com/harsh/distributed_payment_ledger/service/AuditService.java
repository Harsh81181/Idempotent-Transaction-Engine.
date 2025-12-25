package com.harsh.distributed_payment_ledger.service;

import org.springframework.stereotype.Service;

import com.harsh.distributed_payment_ledger.domain.AuditEventType;
import com.harsh.distributed_payment_ledger.domain.AuditLog;
import com.harsh.distributed_payment_ledger.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;

    public void log(
            AuditEventType type,
            String entityType,
            String entityId,
            String message) {

        AuditLog log = AuditLog.builder()
                .eventType(type)
                .entityType(entityType)
                .entityId(entityId)
                .message(message)
                .build();

        auditRepo.save(log);
    }
}

