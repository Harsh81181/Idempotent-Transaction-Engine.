package com.harsh.distributed_payment_ledger.domain;

public enum TransactionStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED;
    
    public boolean canTransitionTo(TransactionStatus target) {
    	return switch(this) {
    	case INITIATED -> target==PROCESSING || target==FAILED;
    	case PROCESSING -> target == SUCCESS || target == FAILED;
        case FAILED -> target == PROCESSING; // retry allowed
        case SUCCESS -> false; // terminal
		default -> throw new IllegalArgumentException("Unexpected value: " + this);
    	};
    }
}