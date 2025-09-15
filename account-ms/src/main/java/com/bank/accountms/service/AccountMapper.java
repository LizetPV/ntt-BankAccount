package com.bank.accountms.service;

import com.bank.accountms.model.Account;
import com.bank.accountms.service.dto.AccountResponse;

public final class AccountMapper {
    private AccountMapper() {}

    public static AccountResponse toResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .customerId(a.getCustomerId())
                .status(a.getStatus().name())
                .type(a.getType().name())
                .balance(a.getBalance())
                .build();
    }
}
