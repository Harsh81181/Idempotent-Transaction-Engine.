package com.harsh.distributed_payment_ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.harsh.distributed_payment_ledger.domain.EntryType;
import com.harsh.distributed_payment_ledger.domain.LedgerEntry;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByAccountId(Long accountId);
    
    @Query("""
            SELECT COALESCE(SUM(le.amount), 0)
            FROM LedgerEntry le
            WHERE le.transactionId = :txnId
              AND le.entryType = :type
        """)
        BigDecimal sumByTransactionAndType(
                @Param("txnId") String txnId,
                @Param("type") EntryType type
        );
    
    @Query("""
            SELECT COALESCE(SUM(
                CASE 
                    WHEN l.entryType = 'CREDIT' THEN l.amount
                    ELSE -l.amount
                END
            ), 0)
            FROM LedgerEntry l
            WHERE l.accountId = :accountId
        """)
        BigDecimal calculateBalanceFromLedger(String accountId);
}

