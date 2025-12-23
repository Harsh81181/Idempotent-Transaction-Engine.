package com.harsh.distributed_payment_ledger.dto;

public record CreateTransactionResponse(
        String transactionId,
        String status
) {}

