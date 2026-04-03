package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResponse {
    private Map<String, String> categories;
    private List<OverspendingFlag> overspending;
    private List<SavingsRecommendation> recommendations;
    private List<SpendingPrediction> predictions;

}