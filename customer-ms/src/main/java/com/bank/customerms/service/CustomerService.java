package com.bank.customerms.service;

import com.bank.customerms.service.dto.CustomerRequest;
import com.bank.customerms.service.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    List<CustomerResponse> findAll();
    CustomerResponse findById(Long id);
    CustomerResponse update(Long id, CustomerRequest request);
    void delete(Long id);

}
