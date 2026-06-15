package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long fromAccountId,
        @NotBlank String toAccountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String description
) {
}
