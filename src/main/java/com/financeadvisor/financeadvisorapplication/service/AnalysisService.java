package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.dto.AnalyzeResponse;
import com.financeadvisor.financeadvisorapplication.dto.OverspendingFlag;
import com.financeadvisor.financeadvisorapplication.dto.SavingsRecommendation;
import com.financeadvisor.financeadvisorapplication.dto.SpendingPrediction;
import com.financeadvisor.financeadvisorapplication.entity.AnalysisData;
import com.financeadvisor.financeadvisorapplication.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;

    public void saveAnalysis(UUID userId, AnalyzeResponse response) throws Exception {
        AnalysisData data = new AnalysisData();
        data.setUserId(userId);
        data.setCategories(objectMapper.writeValueAsString(response.getCategories()));
        data.setOverspending(objectMapper.writeValueAsString(response.getOverspending()));
        data.setRecommendations(objectMapper.writeValueAsString(response.getRecommendations()));
        data.setPredictions(objectMapper.writeValueAsString(response.getPredictions()));
        analysisRepository.save(data);
    }

    public AnalyzeResponse getLatestAnalysis(UUID userId) throws Exception {
        AnalysisData data = analysisRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No analysis found"));

        return new AnalyzeResponse(
                objectMapper.readValue(data.getCategories(), new TypeReference<Map<String, String>>() {}),
                objectMapper.readValue(data.getOverspending(), new TypeReference<List<OverspendingFlag>>() {}),
                objectMapper.readValue(data.getRecommendations(), new TypeReference<List<SavingsRecommendation>>() {}),
                objectMapper.readValue(data.getPredictions(),   new TypeReference<List<SpendingPrediction>>() {})
        );
    }

    public List<AnalyzeResponse> getAllAnalyses(UUID userId) {
        List<AnalysisData> data = analysisRepository.findAllByUserId(userId)
                .orElseThrow();
        // Fix — wrap in try/catch
        return data.stream().map(a -> {
            try {
                return new AnalyzeResponse(
                        objectMapper.readValue(a.getCategories(), new TypeReference<Map<String, String>>() {}),
                        objectMapper.readValue(a.getOverspending(), new TypeReference<List<OverspendingFlag>>() {}),
                        objectMapper.readValue(a.getRecommendations(), new TypeReference<List<SavingsRecommendation>>() {}),
                        objectMapper.readValue(a.getPredictions(), new TypeReference<List<SpendingPrediction>>() {})
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse analysis: " + e.getMessage());
            }
        }).toList();
    }
}