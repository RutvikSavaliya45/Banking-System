package com.example.banking.controller;

import com.example.banking.dto.LoanDecisionRequest;
import com.example.banking.dto.LoanRequest;
import com.example.banking.dto.LoanResponse;
import com.example.banking.service.LoanService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse apply(@Valid @RequestBody LoanRequest request, Principal principal) {
        return loanService.apply(request, principal.getName());
    }

    @GetMapping("/mine")
    public List<LoanResponse> mine(Principal principal) {
        return loanService.mine(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<LoanResponse> all() {
        return loanService.all();
    }

    @PatchMapping("/{loanId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public LoanResponse decide(@PathVariable Long loanId, @Valid @RequestBody LoanDecisionRequest request) {
        return loanService.decide(loanId, request);
    }
}
