package com.bank.customerms.service;

import java.util.concurrent.CompletableFuture;

public interface AccountClient {
    CompletableFuture<Boolean> hasActiveAccounts(Long customerId);
}
