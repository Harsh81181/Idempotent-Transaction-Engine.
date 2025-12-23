package com.harsh.distributed_payment_ledger.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.domain.EntryType;
import com.harsh.distributed_payment_ledger.domain.LedgerEntry;
import com.harsh.distributed_payment_ledger.domain.LedgerStatus;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.repository.LedgerEntryRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PaymentService {
	private final AccountRepository accountRepo;
    private final LedgerEntryRepository ledgerRepo;
    
    @Transactional
    public void debit(long accountId, BigDecimal amount, String txId) {
    	Account ac=accountRepo.findByIdForUpdate(accountId).orElseThrow(()->new RuntimeException("Account not found"));
    	
    	if(ac.getBalance().compareTo(amount)<0) {
    		throw new RuntimeException("Insufficient Balance");
    	}
    	
    	LedgerEntry entry=ledgerRepo.save(LedgerEntry.builder()
    			.accountId(accountId)
    			.amount(amount)
    			.transactionId(txId)
    			.entryType(EntryType.DEBIT)
    			.status(LedgerStatus.SUCCESS)
    			.createdAt(LocalDateTime.now())
    			.build());
    	
    	ledgerRepo.save(entry);
    	ac.setBalance(ac.getBalance().subtract(amount));
    	accountRepo.save(ac);
    }

}
