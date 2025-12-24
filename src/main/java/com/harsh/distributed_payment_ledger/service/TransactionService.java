package com.harsh.distributed_payment_ledger.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.harsh.distributed_payment_ledger.domain.Account;
import com.harsh.distributed_payment_ledger.domain.EntryType;
import com.harsh.distributed_payment_ledger.domain.LedgerEntry;
import com.harsh.distributed_payment_ledger.domain.Transaction;
import com.harsh.distributed_payment_ledger.domain.TransactionStatus;
import com.harsh.distributed_payment_ledger.dto.CreateTransactionRequest;
import com.harsh.distributed_payment_ledger.dto.CreateTransactionResponse;
import com.harsh.distributed_payment_ledger.repository.AccountRepository;
import com.harsh.distributed_payment_ledger.repository.LedgerEntryRepository;
import com.harsh.distributed_payment_ledger.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService {
	private final AccountRepository accountRepo;
	private final TransactionRepository txnRepo;
	private final LedgerService ledgerService;

	@Transactional
	public Transaction processTransaction(CreateTransactionRequest request) {
		return txnRepo.findByExternalTxnId(request.externalTxnId()).map(existing -> {
			if (existing.getStatus() == TransactionStatus.SUCCESS) {
				return existing;
			}
			if (existing.getStatus() == TransactionStatus.PROCESSING) {
				throw new IllegalArgumentException("Transaction is already in processing");
			}
			return retry(existing); // retry in failed case
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
			return execute(txn);
		} catch (DataIntegrityViolationException e) {
			// Another request created it concurrently
	        return txnRepo.findByExternalTxnId(req.externalTxnId())
	                .orElseThrow(() -> e);
		}
	}

	private Transaction retry(Transaction txn) {
		txn.transitionTo(TransactionStatus.PROCESSING);
		txnRepo.save(txn);
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
				throw new IllegalStateException("Insufficient balance");
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
		} catch (Exception e) {
			txn.transitionTo(TransactionStatus.FAILED);
			txnRepo.save(txn);
			throw e;
		}
		return txn;
	}

}
