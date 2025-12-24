package com.harsh.distributed_payment_ledger.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceDrift {

    private String accountId;
    private BigDecimal ledgerBalance;
    private BigDecimal accountBalance;
    private BigDecimal drift;
}

