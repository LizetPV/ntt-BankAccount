package com.bank.customerms.service.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
}
