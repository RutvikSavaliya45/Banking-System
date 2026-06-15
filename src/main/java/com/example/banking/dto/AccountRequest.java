package com.example.banking.dto;

import com.example.banking.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(@NotNull AccountType type) {
}
