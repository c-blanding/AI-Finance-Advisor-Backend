package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private double amount;
    private String description;
    private String category;
    private LocalDate date;
}
