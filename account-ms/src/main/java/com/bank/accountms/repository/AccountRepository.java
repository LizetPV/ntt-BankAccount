package com.bank.accountms.repository;

import com.bank.accountms.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByCustomerIdAndStatus(Long customerId, Account.Status status);
    Optional<Account> findByAccountNumber(String accountNumber);
}