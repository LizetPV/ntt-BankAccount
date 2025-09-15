package com.bank.accountms.controller;

import com.bank.accountms.service.AccountService;
import com.bank.accountms.service.dto.AccountRequest;
import com.bank.accountms.service.dto.AccountResponse;
import com.bank.accountms.service.dto.TransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints to manage bank accounts")
public class AccountController {

    private final AccountService service;

    @Operation(summary = "List accounts")
    @GetMapping
    public List<AccountResponse> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Create account", description = "Creates a new bank account for an existing customer.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody AccountRequest request, HttpServletResponse http) {
        var resp = service.create(request);
        http.setHeader("X-Message", "Account created successfully");
        return resp;
    }

    // -------- Por ID --------
    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Deposit into account (by ID)")
    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id,
                                   @Valid @RequestBody TransactionRequest request,
                                   HttpServletResponse http) {
        var resp = service.deposit(id, request);
        http.setHeader("X-Message", "Deposit completed");
        return resp;
    }

    @Operation(summary = "Withdraw from account (by ID)")
    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id,
                                    @Valid @RequestBody TransactionRequest request,
                                    HttpServletResponse http) {
        var resp = service.withdraw(id, request);
        http.setHeader("X-Message", "Withdrawal completed");
        return resp;
    }

    // -------- Por Número de Cuenta --------
    @Operation(summary = "Get account by number")
    @GetMapping("/number/{accountNumber}")
    public AccountResponse findByAccountNumber(@PathVariable String accountNumber) {
        return service.findByAccountNumber(accountNumber);
    }

    @Operation(summary = "Deposit into account (by number)")
    @PostMapping("/number/{accountNumber}/deposit")
    public AccountResponse depositByNumber(@PathVariable String accountNumber,
                                           @Valid @RequestBody TransactionRequest request,
                                           HttpServletResponse http) {
        var resp = service.depositByAccountNumber(accountNumber, request);
        http.setHeader("X-Message", "Deposit completed");
        return resp;
    }

    @Operation(summary = "Withdraw from account (by number)")
    @PostMapping("/number/{accountNumber}/withdraw")
    public AccountResponse withdrawByNumber(@PathVariable String accountNumber,
                                            @Valid @RequestBody TransactionRequest request,
                                            HttpServletResponse http) {
        var resp = service.withdrawByAccountNumber(accountNumber, request);
        http.setHeader("X-Message", "Withdrawal completed");
        return resp;
    }

    // -------- Otros --------
    @Operation(summary = "Has active accounts", description = "Returns true if the customer has ACTIVE accounts.")
    @GetMapping("/active")
    public boolean hasActive(@RequestParam Long customerId) {
        return service.hasActiveAccounts(customerId);
    }

    @Operation(summary = "Delete account")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
