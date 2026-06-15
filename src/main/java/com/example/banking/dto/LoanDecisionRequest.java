package com.example.banking.dto;

import com.example.banking.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;

public record LoanDecisionRequest(@NotNull LoanStatus status) {
}
