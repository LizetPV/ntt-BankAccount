package com.bank.accountms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Entity
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_number", columnNames = "account_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    public enum Type { SAVINGS, CHECKING }
    public enum Status { ACTIVE, INACTIVE }

    @PrePersist
    void ensureAccountNumber() {
        if (this.accountNumber == null) {
            var base = String.valueOf(System.nanoTime()).replaceAll("\\D","");
            this.accountNumber = base.substring(Math.max(0, base.length() - 12))
                    + String.valueOf((base.hashCode() & 0xffff)).substring(0, 4);
        }
    }
}