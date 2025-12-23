package com.harsh.distributed_payment_ledger.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTransactionRequest(
        @NotBlank String externalTxnId,
        @NotBlank long fromAccount,
        @NotBlank long toAccount,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}

