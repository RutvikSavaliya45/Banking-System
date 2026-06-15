package com.example.banking.controller;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.service.TransferService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public List<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request, Principal principal) {
        return transferService.transfer(request, principal.getName());
    }
}
