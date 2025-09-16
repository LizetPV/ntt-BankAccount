package com.bank.customerms.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String dni;
    @Email @NotBlank private String email;
}
