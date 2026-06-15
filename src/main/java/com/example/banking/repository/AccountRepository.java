package com.example.banking.repository;

import com.example.banking.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwnerEmail(String email);

    Optional<Account> findByIdAndOwnerEmail(Long id, String email);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
