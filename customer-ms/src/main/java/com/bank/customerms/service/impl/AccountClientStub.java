package com.bank.customerms.service.impl;

import com.bank.customerms.service.AccountClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Profile("stub")
public class AccountClientStub implements AccountClient {
    @Override
    public CompletableFuture<Boolean> hasActiveAccounts(Long customerId) {

        return CompletableFuture.completedFuture(false);
    }
}
