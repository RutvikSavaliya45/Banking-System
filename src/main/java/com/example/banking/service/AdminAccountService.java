package com.example.banking.service;

import com.example.banking.dto.AdminAccountDetailResponse;
import com.example.banking.dto.AdminAccountResponse;
import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.LoanResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.AccountStatus;
import com.example.banking.entity.LoanStatus;
import com.example.banking.exception.ApiException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.LoanRepository;
import com.example.banking.repository.TransactionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final AccountService accountService;
    private final LoanService loanService;

    public AdminAccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LoanRepository loanRepository,
            AccountService accountService,
            LoanService loanService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> listAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminAccountDetailResponse getAccountDetails(Long accountId) {
        Account account = findAccount(accountId);
        List<TransactionResponse> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(accountService::toTransactionResponse)
                .toList();
        List<LoanResponse> runningLoans = loanRepository
                .findByBorrowerIdAndStatusOrderByCreatedAtDesc(account.getOwner().getId(), LoanStatus.APPROVED)
                .stream()
                .map(loanService::toResponse)
                .toList();

        return new AdminAccountDetailResponse(toAdminResponse(account), transactions, runningLoans);
    }

    @Transactional
    public AccountResponse freeze(Long accountId) {
        Account account = findAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Closed accounts cannot be frozen");
        }
        account.setStatus(AccountStatus.FROZEN);
        return accountService.toResponse(account);
    }

    @Transactional
    public AccountResponse unfreeze(Long accountId) {
        Account account = findAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Closed accounts cannot be reactivated");
        }
        account.setStatus(AccountStatus.ACTIVE);
        return accountService.toResponse(account);
    }

    private Account findAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AdminAccountResponse toAdminResponse(Account account) {
        return new AdminAccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance(),
                account.getOwner().getId(),
                account.getOwner().getFullName(),
                account.getOwner().getEmail(),
                account.getOwner().getRoles()
        );
    }
}
