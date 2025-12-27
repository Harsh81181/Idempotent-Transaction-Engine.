package com.harsh.distributed_payment_ledger.job;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.domain.AuditEventType;
import com.harsh.distributed_payment_ledger.dto.BalanceDrift;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.service.AuditService;
import com.harsh.distributed_payment_ledger.service.ReconciliationService;

import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class ReconciliationJob {
	private static final BigDecimal MAX_AUTO_REPAIR_DRIFT =new BigDecimal("0.01");
	private final AccountRepository accountRepository;
	private final ReconciliationService service;
	private final AuditService auditService;

	private final Counter driftDetectedCounter;
	private final Counter autoRepairCounter;
	@Scheduled(cron = "0 0 * * * ?")
	public void runReconciliation() {
		List<BalanceDrift> list=service.reconcileAllAcounts();
		
		if(!list.isEmpty()) {
			list.forEach(d->{
				log.info("Drift account found :- "+d);
				driftDetectedCounter.increment();
				auditService.log(
					    AuditEventType.RECONCILIATION_DETECTED,
					    "ACCOUNT",
					    String.valueOf(d.getAccountId()),
					    d.toString()
					);

				if(d.getDrift().abs().compareTo(MAX_AUTO_REPAIR_DRIFT)<=0) {
					repairAccount(d);
					autoRepairCounter.increment();
				}else {
					log.info(
		                    "Large drift detected, manual review required: " + d
		                );
					auditService.log(
						    AuditEventType.RECONCILIATION_MANUAL_REQUIRED,
						    "ACCOUNT",
						    String.valueOf(d.getAccountId()),
						    "Drift too large: " + d.getDrift()
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
		
		auditService.log(
			    AuditEventType.RECONCILIATION_AUTO_REPAIRED,
			    "ACCOUNT",
			    String.valueOf(drift.getAccountId()),
			    "Balance corrected to " + drift.getLedgerBalance()
			);

	}
}
