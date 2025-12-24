package com.harsh.distributed_payment_ledger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "transactions",
    uniqueConstraints = @UniqueConstraint(columnNames = "externalTxnId")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // client-provided idempotency key
    @Column(name = "externalTxnId", nullable = false, unique = true)
    private String externalTxnId;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    public void transitionTo(TransactionStatus newStatus) {
    	if(!this.status.canTransitionTo(newStatus)) {
    		throw new IllegalStateException(
    	            "Invalid transaction state transition: " +
    	            this.status + " → " + newStatus
    	        );
    	}
    	this.status=newStatus;
    }
}

