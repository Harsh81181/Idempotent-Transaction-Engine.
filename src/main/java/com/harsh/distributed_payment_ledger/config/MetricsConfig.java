package com.harsh.distributed_payment_ledger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class MetricsConfig {
	@Bean
	Counter txnSuccess(MeterRegistry registry ) {
		return Counter.builder("Transaction.success")
				.description("Successful Transactions")
				.register(registry);
	}
	@Bean
	Counter txnFailed(MeterRegistry registry ) {
		return Counter.builder("Transaction.failed")
				.description("Failed Transactions")
				.register(registry);
	}
	@Bean
	Counter txnRetried(MeterRegistry registry ) {
		return Counter.builder("Transaction.retried")
				.description("Retried Transactions")
				.register(registry);
	}
	@Bean
	Counter idempotencyHit(MeterRegistry registry ) {
		return Counter.builder("Transaction.idempotency.hit")
				.register(registry);
	}
	@Bean
	Counter ledgerDriftDetected(MeterRegistry registry) {
	    return Counter.builder("ledger.drift.detected").register(registry);
	}

	@Bean
	Counter ledgerAutoRepaired(MeterRegistry registry) {
	    return Counter.builder("ledger.drift.auto_repaired").register(registry);
	}

}
