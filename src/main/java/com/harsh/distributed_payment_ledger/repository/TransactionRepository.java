package com.harsh.distributed_payment_ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harsh.distributed_payment_ledger.domain.Transaction;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByExternalTxnId(String externalTxnId);
}

