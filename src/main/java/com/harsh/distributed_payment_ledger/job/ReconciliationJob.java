package com.harsh.distributed_payment_ledger.job;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.dto.BalanceDrift;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.service.ReconciliationService;

import lombok.AllArgsConstructor;

@Component
@EnableScheduling
@AllArgsConstructor
public class ReconciliationJob {
	private static final BigDecimal MAX_AUTO_REPAIR_DRIFT =new BigDecimal("0.01");
	private final AccountRepository accountRepository;
	private final ReconciliationService service;
	
	@Scheduled(cron = "0 0 * * * ?")
	public void runReconciliation() {
		List<BalanceDrift> list=service.reconcileAllAcounts();
		
		if(!list.isEmpty()) {
			list.forEach(d->{
				System.err.println("Drift account found :- "+d);
				if(d.getDrift().abs().compareTo(MAX_AUTO_REPAIR_DRIFT)<=0) {
					repairAccount(d);
				}else {
					System.err.println(
		                    "Large drift detected, manual review required: " + d
		                );
				}
					}
				);
			
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void repairAccount(BalanceDrift drift) {
		Account ac= accountRepository.findByAccountKey(drift.getAccountId()).
				orElseThrow(()->new RuntimeException("Account not found"));
		ac.setBalance(drift.getLedgerBalance());
		accountRepository.save(ac);
	}
}
