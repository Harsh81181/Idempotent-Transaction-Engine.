package com.harsh.distributed_payment_ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.harsh.distributed_payment_ledger.domain.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Account> findByAccountKey(String accountKey);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(long id);

}
