package com.example.banking.dto;

import com.example.banking.entity.TransactionStatus;
import com.example.banking.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String reference,
        String description,
        Instant createdAt
) {
}
