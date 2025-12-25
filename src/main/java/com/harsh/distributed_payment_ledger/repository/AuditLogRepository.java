package com.harsh.distributed_payment_ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harsh.distributed_payment_ledger.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

