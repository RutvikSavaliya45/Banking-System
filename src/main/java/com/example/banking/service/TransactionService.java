package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    public List<TransactionResponse> history(Long accountId, String email) {
        accountService.findOwnedAccount(accountId, email);
        return transactionRepository.findByAccountIdAndAccountOwnerEmailOrderByCreatedAtDesc(accountId, email).stream()
                .map(accountService::toTransactionResponse)
                .toList();
    }
}
