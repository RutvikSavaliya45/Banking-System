package com.example.banking.dto;

import com.example.banking.entity.LoanStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
        Long id,
        BigDecimal principal,
        BigDecimal annualInterestRate,
        Integer termMonths,
        LoanStatus status,
        BigDecimal estimatedMonthlyPayment,
        Instant createdAt
) {
}
