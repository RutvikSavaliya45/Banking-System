package com.example.banking.service;

import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.DepositWithdrawRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.AccountStatus;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.TransactionStatus;
import com.example.banking.entity.TransactionType;
import com.example.banking.entity.User;
import com.example.banking.exception.ApiException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse create(AccountRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Account account = new Account();
        account.setType(request.type());
        account.setOwner(user);
        account.setAccountNumber(generateAccountNumber());

        return toResponse(accountRepository.save(account));
    }

    public List<AccountResponse> list(String email) {
        return accountRepository.findByOwnerEmail(email).stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse get(Long accountId, String email) {
        return toResponse(findOwnedAccount(accountId, email));
    }

    @Transactional
    public AccountResponse close(Long accountId, String email) {
        Account account = findOwnedAccount(accountId, email);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only zero-balance accounts can be closed");
        }
        account.setStatus(AccountStatus.CLOSED);
        return toResponse(account);
    }

    @Transactional
    public TransactionResponse deposit(Long accountId, DepositWithdrawRequest request, String email) {
        Account account = findOwnedAccount(accountId, email);
        ensureActive(account);
        account.setBalance(account.getBalance().add(request.amount()));
        Transaction transaction = saveTransaction(
                account,
                TransactionType.DEPOSIT,
                request.amount(),
                request.description(),
                UUID.randomUUID().toString()
        );
        return toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long accountId, DepositWithdrawRequest request, String email) {
        Account account = findOwnedAccount(accountId, email);
        ensureActive(account);
        ensureSufficientFunds(account, request.amount());
        account.setBalance(account.getBalance().subtract(request.amount()));
        Transaction transaction = saveTransaction(
                account,
                TransactionType.WITHDRAWAL,
                request.amount(),
                request.description(),
                UUID.randomUUID().toString()
        );
        return toTransactionResponse(transaction);
    }

    Account findOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndOwnerEmail(accountId, email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Account is not active");
        }
    }

    void ensureSufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
    }

    Transaction saveTransaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            String description,
            String reference
    ) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description);
        transaction.setReference(reference);
        return transactionRepository.save(transaction);
    }

    AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance()
        );
    }

    TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getReference(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "BA" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
