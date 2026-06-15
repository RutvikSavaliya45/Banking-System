package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LoanRequest(
        @NotNull @DecimalMin(value = "1000.00") BigDecimal principal,
        @NotNull @DecimalMin(value = "1.00") BigDecimal annualInterestRate,
        @NotNull @Min(1) Integer termMonths
) {
}
