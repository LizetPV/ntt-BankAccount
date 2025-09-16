package com.bank.customerms.service;

import com.bank.customerms.model.Customer;
import com.bank.customerms.service.dto.CustomerRequest;
import com.bank.customerms.service.dto.CustomerResponse;

public final class CustomerMapper {
    private CustomerMapper() {}

    public static Customer toEntity(CustomerRequest r) {
        return Customer.builder()
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .dni(r.getDni())
                .email(r.getEmail())
                .build();
    }

    public static void updateEntity(Customer c, CustomerRequest r) {
        c.setFirstName(r.getFirstName());
        c.setLastName(r.getLastName());
        c.setEmail(r.getEmail());
    }

    public static CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .dni(c.getDni())
                .email(c.getEmail())
                .build();
    }
}
