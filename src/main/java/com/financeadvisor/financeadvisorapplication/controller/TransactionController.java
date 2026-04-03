package com.financeadvisor.financeadvisorapplication.controller;

import com.financeadvisor.financeadvisorapplication.dto.*;
import com.financeadvisor.financeadvisorapplication.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    ResponseEntity<List<TransactionResponse>> getUserTransactions(@RequestAttribute("userId") UUID userId) {
        List<TransactionResponse> response = transactionService.getUserTransactions(userId).stream().map(TransactionResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    ResponseEntity<TransactionResponse> createTransaction(@RequestAttribute("userId") UUID userId, @RequestBody TransactionRequest request) {
       TransactionResponse response = TransactionResponse.from(transactionService.createTransaction(userId, request));
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTransaction(@RequestAttribute("userId") UUID userId, @PathVariable UUID id) {
        transactionService.deleteTransactions(userId, id);
        return ResponseEntity.noContent().build();
    }
}
