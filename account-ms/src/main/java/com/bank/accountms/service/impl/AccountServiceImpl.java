package com.bank.accountms.service.impl;

import com.bank.accountms.model.Account;
import com.bank.accountms.repository.AccountRepository;
import com.bank.accountms.service.AccountMapper;
import com.bank.accountms.service.AccountService;
import com.bank.accountms.service.CustomerClient;
import com.bank.accountms.service.dto.AccountRequest;
import com.bank.accountms.service.dto.AccountResponse;
import com.bank.accountms.service.dto.TransactionRequest;
import com.bank.accountms.service.exception.BusinessException;
import com.bank.accountms.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bank.accountms.service.AccountMapper.toResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("-500"); // límite para CHECKING
    private static final long CUSTOMER_LOOKUP_TIMEOUT_SECONDS = 3;

    private final AccountRepository repository;
    private final CustomerClient customerClient;

    // ----------------------------
    // Crear / Listar / Obtener por ID
    // ----------------------------
    @Override
    public AccountResponse create(AccountRequest r) {
        if (r.getCustomerId() == null) throw new BusinessException("customerId is required");
        if (r.getType() == null) throw new BusinessException("type is required");
        if (r.getInitialBalance() == null || r.getInitialBalance().compareTo(ZERO) <= 0) {
            throw new BusinessException("initialBalance must be > 0");
        }

        // validar cliente en CustomerMS
        boolean exists;
        try {
            exists = customerClient.existsCustomer(r.getCustomerId())
                    .get(CUSTOMER_LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new BusinessException("Customer service timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Customer service interrupted", e);
        } catch (ExecutionException e) {
            throw new BusinessException("Customer service unavailable", e.getCause());
        }
        if (!exists) throw new NotFoundException("Customer not found");

        Account.Type type;
        try {
            type = Account.Type.valueOf(r.getType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid account type. Allowed: SAVINGS, CHECKING");
        }

        Account a = Account.builder()
                .customerId(r.getCustomerId())
                .type(type)
                .status(Account.Status.ACTIVE)
                .balance(r.getInitialBalance())
                .build();

        return toResponse(repository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        return repository.findAll().stream().map(AccountMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(Long id) {
        Account a = repository.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
        return toResponse(a);
    }

    // ----------------------------
    // Operaciones por ID
    // ----------------------------
    @Override
    public AccountResponse deposit(Long id, TransactionRequest r) {
        if (r.getAmount() == null || r.getAmount().signum() <= 0)
            throw new BusinessException("Amount must be greater than zero");

        Account a = repository.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
        assertActive(a);

        a.setBalance(a.getBalance().add(r.getAmount()));
        return toResponse(repository.save(a));
    }

    @Override
    public AccountResponse withdraw(Long id, TransactionRequest r) {
        if (r.getAmount() == null || r.getAmount().signum() <= 0)
            throw new BusinessException("Amount must be greater than zero");

        Account a = repository.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
        assertActive(a);

        BigDecimal newBalance = a.getBalance().subtract(r.getAmount());
        switch (a.getType()) {
            case SAVINGS -> {
                if (newBalance.compareTo(ZERO) < 0) throw new BusinessException("Insufficient funds");
            }
            case CHECKING -> {
                if (newBalance.compareTo(OVERDRAFT_LIMIT) < 0)
                    throw new BusinessException("Overdraft limit exceeded (-500)");
            }
        }
        a.setBalance(newBalance);
        return toResponse(repository.save(a));
    }

    // ----------------------------
    // Operaciones por accountNumber
    // ----------------------------
    @Override
    @Transactional(readOnly = true)
    public AccountResponse findByAccountNumber(String accountNumber) {
        return toResponse(getByAccountNumberOrThrow(accountNumber));
    }

    @Override
    public AccountResponse depositByAccountNumber(String accountNumber, TransactionRequest r) {
        if (r.getAmount() == null || r.getAmount().signum() <= 0)
            throw new BusinessException("Amount must be greater than zero");

        Account a = getByAccountNumberOrThrow(accountNumber);
        assertActive(a);

        a.setBalance(a.getBalance().add(r.getAmount()));
        return toResponse(repository.save(a));
    }

    @Override
    public AccountResponse withdrawByAccountNumber(String accountNumber, TransactionRequest r) {
        if (r.getAmount() == null || r.getAmount().signum() <= 0)
            throw new BusinessException("Amount must be greater than zero");

        Account a = getByAccountNumberOrThrow(accountNumber);
        assertActive(a);

        BigDecimal newBalance = a.getBalance().subtract(r.getAmount());
        switch (a.getType()) {
            case SAVINGS -> {
                if (newBalance.compareTo(ZERO) < 0) throw new BusinessException("Insufficient funds");
            }
            case CHECKING -> {
                if (newBalance.compareTo(OVERDRAFT_LIMIT) < 0)
                    throw new BusinessException("Overdraft limit exceeded (-500)");
            }
        }
        a.setBalance(newBalance);
        return toResponse(repository.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveAccounts(Long customerId) {
        return repository.existsByCustomerIdAndStatus(customerId, Account.Status.ACTIVE);
    }

    @Override
    public void delete(Long id) {
        var acc = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (acc.getBalance() != null && acc.getBalance().signum() != 0) {
            throw new BusinessException("Balance must be zero to delete the account");
        }
        repository.delete(acc);
    }

    private Account getByAccountNumberOrThrow(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private void assertActive(Account a) {
        if (a.getStatus() != Account.Status.ACTIVE) {
            throw new BusinessException("Account is not ACTIVE");
        }
    }
}
