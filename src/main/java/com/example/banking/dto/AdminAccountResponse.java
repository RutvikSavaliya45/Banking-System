package com.example.banking.dto;

import com.example.banking.entity.AccountStatus;
import com.example.banking.entity.AccountType;
import com.example.banking.entity.Role;
import java.math.BigDecimal;
import java.util.Set;

public record AdminAccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        Set<Role> ownerRoles
) {
}
