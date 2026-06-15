package com.example.banking.repository;

import com.example.banking.entity.Loan;
import com.example.banking.entity.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerEmailOrderByCreatedAtDesc(String email);

    List<Loan> findByBorrowerIdAndStatusOrderByCreatedAtDesc(Long borrowerId, LoanStatus status);
}
