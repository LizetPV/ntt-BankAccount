package com.bank.accountms.service;

import com.bank.accountms.service.dto.AccountRequest;
import com.bank.accountms.service.dto.AccountResponse;
import com.bank.accountms.service.dto.TransactionRequest;

import java.util.List;

public interface AccountService {
    AccountResponse create(AccountRequest request);
    List<AccountResponse> findAll();
    AccountResponse findById(Long id);
    AccountResponse deposit(Long id, TransactionRequest request);
    AccountResponse withdraw(Long id, TransactionRequest request);

    AccountResponse findByAccountNumber(String accountNumber);
    AccountResponse depositByAccountNumber(String accountNumber, TransactionRequest request);
    AccountResponse withdrawByAccountNumber(String accountNumber, TransactionRequest request);

    boolean hasActiveAccounts(Long customerId);
    void delete(Long id);

}
