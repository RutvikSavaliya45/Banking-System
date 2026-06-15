package com.example.banking.dto;

import java.util.List;

public record AdminAccountDetailResponse(
        AdminAccountResponse account,
        List<TransactionResponse> transactions,
        List<LoanResponse> runningLoans
) {
}
