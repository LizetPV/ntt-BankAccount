package com.bank.customerms.repository;

import com.bank.customerms.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByDni(String dni);
    Optional<Customer> findByDni(String dni);
}
