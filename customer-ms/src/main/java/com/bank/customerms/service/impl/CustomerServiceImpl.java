package com.bank.customerms.service.impl;

import com.bank.customerms.model.Customer;
import com.bank.customerms.repository.CustomerRepository;
import com.bank.customerms.service.AccountClient;
import com.bank.customerms.service.CustomerMapper;
import com.bank.customerms.service.CustomerService;
import com.bank.customerms.service.dto.CustomerRequest;
import com.bank.customerms.service.dto.CustomerResponse;
import com.bank.customerms.service.exception.BusinessException;
import com.bank.customerms.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static com.bank.customerms.service.CustomerMapper.toEntity;
import static com.bank.customerms.service.CustomerMapper.toResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final long ACCOUNT_CLIENT_TIMEOUT_SECONDS = 3L;

    private final CustomerRepository repository;

    private final AccountClient accountClient;

    @Override
    public CustomerResponse create(CustomerRequest request) {

        var normalized = request.toBuilder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .dni(request.getDni().trim())
                .email(request.getEmail().trim().toLowerCase())
                .build();

        if (repository.existsByDni(normalized.getDni())) {
            throw new DataIntegrityViolationException("DNI already exists");
        }
        return toResponse(repository.save(toEntity(normalized)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return repository.findById(id)
                .map(CustomerMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        var customer = requireCustomer(id);

        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setEmail(request.getEmail().trim().toLowerCase());

        return toResponse(repository.save(customer));
    }

    @Override
    public void delete(Long id) {
        var customer = requireCustomer(id);

        boolean hasActive = safeHasActiveAccounts(id);
        if (hasActive) {
            throw new BusinessException("Cannot delete: customer has active accounts");
        }
        repository.delete(customer);
    }

    private Customer requireCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private boolean safeHasActiveAccounts(Long customerId) {
        try {
            return accountClient.hasActiveAccounts(customerId)
                    .orTimeout(ACCOUNT_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> { throw new BusinessException("Account service unavailable", ex); })
                    .join();
        } catch (CompletionException ce) {
            if (ce.getCause() instanceof BusinessException be) {
                throw be; // re-lanza la BusinessException original
            }
            throw new BusinessException("Account service unavailable", ce.getCause());
        }
    }
}
