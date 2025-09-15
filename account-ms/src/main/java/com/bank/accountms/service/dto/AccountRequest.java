package com.bank.accountms.service.dto;
// Anotaciones de validación de Bean Validation (Jakarta Validation)

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Lombok genera automáticamente getters/setters, constructores y builder
import lombok.*;

import java.math.BigDecimal;
/**
 * DTO de entrada para crear una cuenta.
 * Se usa en el endpoint POST /accounts como request body.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountRequest {
    /**
     * ID del cliente dueño de la cuenta.
     * @NotNull -> obliga a que el campo venga en el request (si falta, 400 Bad Request).
     * (Opcional) se podría agregar @Positive para exigir > 0.
     */
    @NotNull private Long customerId;
    /**
     * Tipo de cuenta. El contrato dice: "SAVINGS" | "CHECKING".
     */
    @NotNull private String type;              // "SAVINGS" | "CHECKING"

    /**
     * Saldo inicial.
     * @Min(0) -> permite 0 o más.
     */
    @Min(0)
    @Min(0)  private BigDecimal initialBalance;

}
