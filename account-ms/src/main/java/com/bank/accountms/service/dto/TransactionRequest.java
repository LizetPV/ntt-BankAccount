package com.bank.accountms.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionRequest {
    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;
}
