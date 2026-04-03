package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingPrediction {
    private String category;
    private Double currentSpend;
    private Double predictedAmount;
    private Double historicalAverage;
    private Boolean onTrack;
    private Double variance;
    private String alert;
}