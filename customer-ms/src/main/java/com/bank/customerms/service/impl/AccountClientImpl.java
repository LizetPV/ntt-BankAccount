package com.bank.customerms.service.impl;

import com.bank.customerms.service.AccountClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Primary
public class AccountClientImpl implements AccountClient {

    private final RestTemplate restTemplate;

    @Value("${accountms.base-url:http://localhost:8082}")
    private String accountMsBaseUrl;

    @Override
    public CompletableFuture<Boolean> hasActiveAccounts(Long customerId) {
        String url = accountMsBaseUrl + "/accounts/active?customerId=" + customerId;
        try {
            ResponseEntity<Boolean> resp = restTemplate.getForEntity(url, Boolean.class);
            Boolean body = resp.getBody();
            return CompletableFuture.completedFuture(Boolean.TRUE.equals(body));
        } catch (Exception ex) {

            CompletableFuture<Boolean> f = new CompletableFuture<>();
            f.completeExceptionally(ex);
            return f;
        }
    }
}
