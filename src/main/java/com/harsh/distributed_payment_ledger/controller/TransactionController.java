package com.harsh.distributed_payment_ledger.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harsh.distributed_payment_ledger.domain.Transaction;
import com.harsh.distributed_payment_ledger.dto.CreateTransactionRequest;
import com.harsh.distributed_payment_ledger.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/transaction")
    public Transaction create(@RequestBody @Valid CreateTransactionRequest req) {
        return service.processTransaction(req);
    }
}
