package com.example.banking.dto;

import com.example.banking.entity.AccountStatus;
import com.example.banking.entity.AccountType;
import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        AccountStatus status,
        BigDecimal balance
) {
}
