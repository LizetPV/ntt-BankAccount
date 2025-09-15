package com.bank.accountms.service.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String status;  // "ACTIVE" | "INACTIVE"
    private String type;    // "SAVINGS" | "CHECKING"
    private BigDecimal balance;
}
