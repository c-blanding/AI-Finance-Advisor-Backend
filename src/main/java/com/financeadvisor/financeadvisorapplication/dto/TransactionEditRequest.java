package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEditRequest {
    private UUID transactionId;
    private double amount;
    private String description;
    private String category;
    private LocalDate date;
}
