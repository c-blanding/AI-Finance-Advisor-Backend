package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverspendingFlag {
    private String category;
    private Double currentSpend;
    private Double averageSpend;
    private String severity;
    private String insight;
}