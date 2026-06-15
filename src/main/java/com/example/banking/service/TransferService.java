package com.example.banking.service;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.entity.Account;
import com.example.banking.entity.TransactionType;
import com.example.banking.exception.ApiException;
import com.example.banking.repository.AccountRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public TransferService(AccountRepository accountRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Transactional
    public List<TransactionResponse> transfer(TransferRequest request, String email) {
        Account from = accountService.findOwnedAccount(request.fromAccountId(), email);
        Account to = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (from.getId().equals(to.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account");
        }

        accountService.ensureActive(from);
        accountService.ensureActive(to);
        accountService.ensureSufficientFunds(from, request.amount());

        String reference = UUID.randomUUID().toString();
        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));

        return List.of(
                accountService.toTransactionResponse(accountService.saveTransaction(
                        from,
                        TransactionType.TRANSFER_OUT,
                        request.amount(),
                        request.description(),
                        reference
                )),
                accountService.toTransactionResponse(accountService.saveTransaction(
                        to,
                        TransactionType.TRANSFER_IN,
                        request.amount(),
                        request.description(),
                        reference
                ))
        );
    }
}
