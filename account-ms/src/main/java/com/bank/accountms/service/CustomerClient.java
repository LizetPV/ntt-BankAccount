package com.bank.accountms.service;

import java.util.concurrent.CompletableFuture;

public interface CustomerClient {
    CompletableFuture<Boolean> existsCustomer(Long customerId);
}
