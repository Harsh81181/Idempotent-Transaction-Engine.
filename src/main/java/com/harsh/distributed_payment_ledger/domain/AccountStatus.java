package com.harsh.distributed_payment_ledger.domain;

public enum AccountStatus {
    ACTIVE,     // Normal operations allowed
    SUSPENDED,  // Temporarily blocked
    CLOSED      // Permanently closed
}

