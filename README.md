# Idempotent Transaction Engine

A production-grade **Idempotent Transaction Engine** built with Spring Boot, designed to safely process monetary transfers with strong guarantees around **idempotency, consistency, concurrency control, and auditability**.

This project demonstrates how real-world payment systems handle retries, failures, race conditions, and reconciliation at scale.

---

## Key Features

* **Idempotent transaction processing**

  * Client-provided external transaction ID
  * Safe retries without double-debit or double-credit
* **Double-entry ledger system**

  * Every transaction generates balanced debit and credit entries
* **Strong consistency**

  * Pessimistic database locking
  * Atomic balance updates
* **Transaction state machine**

  * Explicit, validated status transitions
* **Concurrency-safe transfers**

  * Deadlock prevention via deterministic lock ordering
* **Automated reconciliation**

  * Detects balance drift between account table and ledger
  * Auto-repair for small discrepancies
* **Audit-friendly design**

  * Ledger as immutable source of truth
* **Dockerized deployment**

  * Multi-stage Docker build
  * Docker Compose with PostgreSQL

---

## High-Level Architecture

```
Client
  |
  |  (externalTxnId)
  v
TransactionService
  |
  |-- Idempotency Check
  |-- State Transition Validation
  |-- Account Locking (PESSIMISTIC_WRITE)
  |-- Ledger Entries (Debit / Credit)
  |-- Balance Update
  |-- Commit / Rollback
  v
MySQL
```

---

## Core Domain Concepts

### Transaction

Represents a client-initiated transfer with lifecycle control.

**States**

* INITIATED
* PROCESSING
* SUCCESS
* FAILED

State transitions are strictly validated to prevent invalid flows.

---

### Ledger Entry

Immutable record of money movement.

* Debit entry (from account)
* Credit entry (to account)
* Ledger is the source of truth

---

### Account

Represents current balance derived from ledger.

* Balance updates are atomic
* Protected by pessimistic locking
* Periodically reconciled against ledger

---

## Idempotency Strategy

* Each transaction requires an `externalTxnId`
* Duplicate requests return the existing transaction result
* Safe retries supported after failures
* No duplicate ledger entries are ever created

---

## Concurrency Control

* **Pessimistic locking** on accounts
* **Deterministic lock ordering** by account ID
* Prevents race conditions and deadlocks
* Ensures serializable behavior for transfers

---

## Reconciliation & Repair

A scheduled reconciliation job:

* Compares account balances with ledger totals
* Detects balance drift
* Automatically repairs small discrepancies
* Flags large drifts for manual review

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven

---


## Database Schema Overview

* `accounts`
* `transactions`
* `ledger_entries`

Ledger entries are append-only and never updated.

---

## Why This Project Exists

This project was built to:

* Model real payment system behavior
* Demonstrate production-level backend design
* Show understanding of idempotency, consistency, and failure handling
* Serve as a foundation for building distributed financial systems

---

## Possible Extensions

* Redis-backed idempotency cache
* Outbox pattern for event publishing
* Kafka integration
* Sharding and partitioned ledgers
* Multi-currency support
* Role-based access control
* Observability (metrics, tracing)

---

## Author

Harsh Bhardwaj
Just say the word.
