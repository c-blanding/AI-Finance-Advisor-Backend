package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingsRecommendation {
    private String category;
    private String suggestion;
    private Double potentialSaving;
    private String difficulty;
    private String timeToImpact;
}