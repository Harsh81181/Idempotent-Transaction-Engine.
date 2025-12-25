package com.harsh.distributed_payment_ledger.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.dto.BalanceDrift;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.repository.LedgerEntryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReconciliationService {
	private final LedgerEntryRepository entryRepository;
	private final AccountRepository accountRepository;
	
	@Transactional(readOnly = true)
	public List<BalanceDrift> reconcileAllAcounts(){
		List<Account> accounts= accountRepository.findAll();
		List<BalanceDrift> drifts=new ArrayList<>();
		for(Account ac:accounts) {
			BigDecimal ledgerBalance= entryRepository.calculateBalanceFromLedger(ac.getAccountKey());
			if(ledgerBalance.compareTo(ac.getBalance())!=0) {
				drifts.add(new BalanceDrift(ac.getAccountKey(), 
											ledgerBalance, 
											ac.getBalance(), 
											ledgerBalance.subtract(ac.getBalance())
											));
			}
		}
		return drifts;
	}
}
