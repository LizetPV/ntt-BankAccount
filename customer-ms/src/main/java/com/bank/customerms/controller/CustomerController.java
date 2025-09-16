package com.bank.customerms.controller;

import com.bank.customerms.service.CustomerService;
import com.bank.customerms.service.dto.CustomerRequest;
import com.bank.customerms.service.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Operations related to customer management")
public class CustomerController {

    private final CustomerService service;

    @Operation(summary = "Create customer", description = "Registers a new customer validating that the DNI is unique.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @Operation(summary = "List customers", description = "Retrieves all registered customers.")
    @GetMapping
    public List<CustomerResponse> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by its unique identifier.")
    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Update customer", description = "Updates name, last name and email of an existing customer.")
    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete customer", description = "Deletes a customer, but blocks the operation if they have active accounts.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
