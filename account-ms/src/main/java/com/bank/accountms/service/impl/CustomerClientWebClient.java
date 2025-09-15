package com.bank.accountms.service.impl;

import com.bank.accountms.service.CustomerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CustomerClientWebClient implements CustomerClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${customerms.base-url:http://localhost:8081}")
    private String customerMsBaseUrl;

    @Override
    public CompletableFuture<Boolean> existsCustomer(Long customerId) {
        return webClientBuilder.build()
                .get()
                .uri(customerMsBaseUrl + "/customers/{id}", customerId)
                .retrieve()
                .toBodilessEntity()
                .map(resp -> true)                 // 2xx → existe
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return reactor.core.publisher.Mono.just(false); // 404 → no existe
                    }
                    return reactor.core.publisher.Mono.error(ex);       // otros errores → propaga
                })
                .toFuture();
    }
}
