package com.harsh.distributed_payment_ledger.exception;

public class DuplicateTransactionException extends RuntimeException{
	public DuplicateTransactionException(String msg) {
		super(msg);
	}
}
