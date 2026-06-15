package com.example.banking.service;

import com.example.banking.dto.LoanDecisionRequest;
import com.example.banking.dto.LoanRequest;
import com.example.banking.dto.LoanResponse;
import com.example.banking.entity.Loan;
import com.example.banking.entity.LoanStatus;
import com.example.banking.entity.User;
import com.example.banking.exception.ApiException;
import com.example.banking.repository.LoanRepository;
import com.example.banking.repository.UserRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LoanResponse apply(LoanRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Loan loan = new Loan();
        loan.setBorrower(user);
        loan.setPrincipal(request.principal());
        loan.setAnnualInterestRate(request.annualInterestRate());
        loan.setTermMonths(request.termMonths());
        return toResponse(loanRepository.save(loan));
    }

    public List<LoanResponse> mine(String email) {
        return loanRepository.findByBorrowerEmailOrderByCreatedAtDesc(email).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LoanResponse> all() {
        return loanRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LoanResponse decide(Long loanId, LoanDecisionRequest request) {
        if (request.status() != LoanStatus.APPROVED && request.status() != LoanStatus.REJECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Decision status must be APPROVED or REJECTED");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan not found"));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending loans can be decided");
        }
        loan.setStatus(request.status());
        return toResponse(loan);
    }

    public LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getPrincipal(),
                loan.getAnnualInterestRate(),
                loan.getTermMonths(),
                loan.getStatus(),
                monthlyPayment(loan),
                loan.getCreatedAt()
        );
    }

    private BigDecimal monthlyPayment(Loan loan) {
        BigDecimal monthlyRate = loan.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return loan.getPrincipal()
                    .divide(BigDecimal.valueOf(loan.getTermMonths()), 2, RoundingMode.HALF_UP);
        }

        double rate = monthlyRate.doubleValue();
        double denominator = 1 - Math.pow(1 + rate, -loan.getTermMonths());
        return loan.getPrincipal()
                .multiply(BigDecimal.valueOf(rate / denominator))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
