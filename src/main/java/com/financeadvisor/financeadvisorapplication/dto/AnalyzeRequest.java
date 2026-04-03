package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeRequest {
    private UUID userId;
    private List<TransactionDTO> currentTransactions;
    private List<TransactionDTO> historicalTransactions;
}
