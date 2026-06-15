package com.example.banking.controller;

import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.DepositWithdrawRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody AccountRequest request, Principal principal) {
        return accountService.create(request, principal.getName());
    }

    @GetMapping
    public List<AccountResponse> list(Principal principal) {
        return accountService.list(principal.getName());
    }

    @GetMapping("/{accountId}")
    public AccountResponse get(@PathVariable Long accountId, Principal principal) {
        return accountService.get(accountId, principal.getName());
    }

    @DeleteMapping("/{accountId}")
    public AccountResponse close(@PathVariable Long accountId, Principal principal) {
        return accountService.close(accountId, principal.getName());
    }

    @PostMapping("/{accountId}/deposit")
    public TransactionResponse deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositWithdrawRequest request,
            Principal principal
    ) {
        return accountService.deposit(accountId, request, principal.getName());
    }

    @PostMapping("/{accountId}/withdraw")
    public TransactionResponse withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositWithdrawRequest request,
            Principal principal
    ) {
        return accountService.withdraw(accountId, request, principal.getName());
    }
}
