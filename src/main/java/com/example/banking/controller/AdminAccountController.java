package com.example.banking.controller;

import com.example.banking.dto.AdminAccountDetailResponse;
import com.example.banking.dto.AdminAccountResponse;
import com.example.banking.dto.AccountResponse;
import com.example.banking.service.AdminAccountService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping
    public List<AdminAccountResponse> listAllAccounts() {
        return adminAccountService.listAllAccounts();
    }

    @GetMapping("/{accountId}")
    public AdminAccountDetailResponse getAccountDetails(@PathVariable Long accountId) {
        return adminAccountService.getAccountDetails(accountId);
    }

    @PatchMapping("/{accountId}/freeze")
    public AccountResponse freeze(@PathVariable Long accountId) {
        return adminAccountService.freeze(accountId);
    }

    @PatchMapping("/{accountId}/unfreeze")
    public AccountResponse unfreeze(@PathVariable Long accountId) {
        return adminAccountService.unfreeze(accountId);
    }
}
