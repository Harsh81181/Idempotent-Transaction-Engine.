package com.harsh.distributed_payment_ledger.domain;

public enum LedgerStatus {
    PENDING,   // Transaction initiated, not finalized
    SUCCESS,   // Applied successfully
    FAILED     // Rolled back or rejected
}

