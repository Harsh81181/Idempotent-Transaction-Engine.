package com.harsh.distributed_payment_ledger.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.domain.AuditEventType;
import com.harsh.distributed_payment_ledger.domain.EntryType;
import com.harsh.distributed_payment_ledger.domain.LedgerEntry;
import com.harsh.distributed_payment_ledger.domain.Transaction;
import com.harsh.distributed_payment_ledger.domain.TransactionStatus;
import com.harsh.distributed_payment_ledger.dto.CreateTransactionRequest;
import com.harsh.distributed_payment_ledger.dto.CreateTransactionResponse;
import com.harsh.distributed_payment_ledger.exception.InsufficientBalanceException;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.repository.LedgerEntryRepository;
import com.harsh.distributed_payment_ledger.repository.TransactionRepository;

import io.micrometer.core.instrument.Counter;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {
	private final AccountRepository accountRepo;
	private final TransactionRepository txnRepo;
	private final LedgerService ledgerService;
	private final AuditService auditService;

	private final Counter txnSuccess;
	private final Counter txnFailed;
	private final Counter txnRetried;
	private final Counter idempotencyHit;
	
	
	@Transactional
	public Transaction processTransaction(CreateTransactionRequest request) {
		return txnRepo.findByExternalTxnId(request.externalTxnId()).map(existing -> {
			idempotencyHit.increment();
			if (existing.getStatus() == TransactionStatus.SUCCESS) {
				log.info("Transaction already completed. "+existing);
				return existing;
			}
			if (existing.getStatus() == TransactionStatus.PROCESSING) {
				log.info("Transaction is already in processing");
				throw new IllegalArgumentException("Transaction is already in processing");
			}
			txnRetried.increment();
			return retry(existing);
		}).orElseGet(() -> createAndExecute(request));
	}

	private Transaction createAndExecute(CreateTransactionRequest req) {
		try {
			Transaction txn = txnRepo.save(Transaction.builder()
					.externalTxnId(req.externalTxnId())
					.amount(req.amount())
					.fromAccountId(req.fromAccount())
					.toAccountId(req.toAccount())
					.status(TransactionStatus.INITIATED)
					.build());
			
			auditService.log(
				    AuditEventType.TRANSACTION_INITIATED,
				    "TRANSACTION",
				    txn.getExternalTxnId(),
				    "Transaction initiated"
				);
			log.info("Transaction Initiated txnid :- "+txn.getExternalTxnId() );
			return execute(txn);
		} catch (DataIntegrityViolationException e) {
			// Another request created it concurrently
			log.info("Another transaction request created it concurrently");
	        return txnRepo.findByExternalTxnId(req.externalTxnId())
	                .orElseThrow(() -> e);
		}
	}

	private Transaction retry(Transaction txn) {
		txn.transitionTo(TransactionStatus.PROCESSING);
		txnRepo.save(txn);
		log.info("Transaction retried - "+txn.getExternalTxnId());
		return execute(txn);
	}

	private Transaction execute(Transaction txn) {
		try {
			if (txn.getFromAccountId().equals(txn.getToAccountId())) {
				throw new IllegalArgumentException("Self-transfer not allowed");
			}
			if (txn.getStatus() != TransactionStatus.PROCESSING) {
				txn.transitionTo(TransactionStatus.PROCESSING);
				txnRepo.save(txn);
			}
			Account from;
			Account to;
			if (txn.getFromAccountId() < txn.getToAccountId()) {
				from = accountRepo.findByIdForUpdate(txn.getFromAccountId())
						.orElseThrow(()->new RuntimeException("Sender Account not found"));
				to = accountRepo.findByIdForUpdate(txn.getToAccountId())
						.orElseThrow(()->new RuntimeException("Reciever Account not found"));
			} else {
				to = accountRepo.findByIdForUpdate(txn.getToAccountId())
						.orElseThrow(()->new RuntimeException("Reciever Account not found"));
				from = accountRepo.findByIdForUpdate(txn.getFromAccountId())
						.orElseThrow(()->new RuntimeException("Sender Account not found"));
			}

			if (from.getBalance().compareTo(txn.getAmount()) < 0) {
				throw new InsufficientBalanceException("Insufficient balance ");
			}

			ledgerService.debit(txn.getExternalTxnId(), txn.getFromAccountId(), txn.getAmount());
			ledgerService.credit(txn.getExternalTxnId(), txn.getToAccountId(), txn.getAmount());

			from.setBalance(from.getBalance().subtract(txn.getAmount()));
			to.setBalance(to.getBalance().add(txn.getAmount()));

			ledgerService.verifyLedgerBalance(txn.getExternalTxnId());

			accountRepo.save(from);
			accountRepo.save(to);

			txn.transitionTo(TransactionStatus.SUCCESS);
			txnRepo.save(txn);
			txnSuccess.increment();
		}catch(InsufficientBalanceException e) {
			txn.transitionTo(TransactionStatus.FAILED);
			txnRepo.save(txn);
			log.info(e.getMessage());
			throw e;
		}
		
		catch (Exception e) {
			txn.transitionTo(TransactionStatus.FAILED);
			txnRepo.save(txn);
			auditService.log(
				    AuditEventType.TRANSACTION_FAILED,
				    "TRANSACTION",
				    txn.getExternalTxnId(),
				    "Transaction failed: " + e.getMessage()
				);
			log.info("Transaction Failed - "+txn.getExternalTxnId());
			txnFailed.increment();
			throw e;
		}
		auditService.log(
			    AuditEventType.TRANSACTION_SUCCESS,
			    "TRANSACTION",
			    txn.getExternalTxnId(),
			    "Transaction completed successfully"
			);
		log.info("Transaction completed successfully - "+txn.getExternalTxnId());
		return txn;
	}

}
