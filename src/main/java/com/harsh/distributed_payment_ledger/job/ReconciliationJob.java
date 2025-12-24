package com.harsh.distributed_payment_ledger.job;

import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.harsh.distributed_payment_ledger.dto.BalanceDrift;
import com.harsh.distributed_payment_ledger.service.ReconciliationService;

import lombok.AllArgsConstructor;

@Component
@EnableScheduling
@AllArgsConstructor
public class ReconciliationJob {

	private final ReconciliationService service;
	
	@Scheduled(cron = "0 0 * * * ?")
	public void runReconciliation() {
		List<BalanceDrift> list=service.reconcileAllAcounts();
		
		if(!list.isEmpty()) {
			list.forEach(d->System.err.println("Balance drift detected: " + d));
		}
	}
}
