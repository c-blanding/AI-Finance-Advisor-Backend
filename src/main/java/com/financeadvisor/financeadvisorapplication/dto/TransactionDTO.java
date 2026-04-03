package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;
    private double amount;
    private String description;
    private String date;
    private String category;
    private String source;
}