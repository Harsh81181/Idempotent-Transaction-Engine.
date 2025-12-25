package com.harsh.distributed_payment_ledger.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.harsh.distributed_payment_ledger.domain.AuditEventType;
import com.harsh.distributed_payment_ledger.domain.EntryType;
import com.harsh.distributed_payment_ledger.domain.LedgerEntry;
import com.harsh.distributed_payment_ledger.repository.LedgerEntryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LedgerService {
	private final LedgerEntryRepository ledgerEntryRepository;
	private final AuditService auditService;

	
	public void debit(String txnId,long accountId,BigDecimal ammount) {
		ledgerEntryRepository.save(LedgerEntry.builder()
									.accountId(accountId)
									.transactionId(txnId)
									.amount(ammount)
									.entryType(EntryType.DEBIT)
									.createdAt(LocalDateTime.now())
									.build());
		auditService.log(
			    AuditEventType.LEDGER_DEBIT,
			    "ACCOUNT",
			    String.valueOf(accountId),
			    "Debited " + ammount + " for txn " + txnId
			);

	}
	
	public void credit(String txnId,long accountId,BigDecimal ammount) {
		ledgerEntryRepository.save(LedgerEntry.builder()
				.accountId(accountId)
				.transactionId(txnId)
				.amount(ammount)
				.entryType(EntryType.CREDIT)
				.createdAt(LocalDateTime.now())
				.build());
		
		auditService.log(
			    AuditEventType.LEDGER_CREDIT,
			    "ACCOUNT",
			    String.valueOf(accountId),
			    "Debited " + ammount + " for txn " + txnId
			);
	}
	
	public void verifyLedgerBalance(String txnId) {
		BigDecimal debit=ledgerEntryRepository.sumByTransactionAndType(txnId, EntryType.DEBIT);
		BigDecimal credit=ledgerEntryRepository.sumByTransactionAndType(txnId, EntryType.CREDIT);
		
		if(debit.compareTo(credit)!=0) 
			throw new IllegalStateException("Ledger imbalance detected for txnId=" + txnId);	
		
	}
}
