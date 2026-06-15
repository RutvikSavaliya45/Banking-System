package com.example.banking.repository;

import com.example.banking.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdAndAccountOwnerEmailOrderByCreatedAtDesc(Long accountId, String email);

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
