package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.dto.AnalyzeRequest;
import com.financeadvisor.financeadvisorapplication.dto.AnalyzeResponse;
import com.financeadvisor.financeadvisorapplication.dto.TransactionDTO;
import com.financeadvisor.financeadvisorapplication.entity.UserTransaction;
import com.financeadvisor.financeadvisorapplication.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    private final AnalysisService analysisService;
    @Value("${ai.service.url}")
    private String aiServiceUrl;


    public AnalyzeResponse analyzeSpending(UUID userId) throws Exception {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate lastMonth = now.minusMonths(2);
        LocalDate threeMonthsAgo = now.minusMonths(3);

        // Fetch current month
        List<UserTransaction> current = transactionRepository
                .findByUserIdAndDateBetween(userId, lastMonth, now);

        // Fetch last 3 months historical
        List<UserTransaction> historical = transactionRepository
                .findByUserIdAndDateBetween(userId, threeMonthsAgo, startOfMonth);

        // Map to DTOs
        List<TransactionDTO> currentDTOs = current.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<TransactionDTO> historicalDTOs = historical.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        // Build request
        AnalyzeRequest request = new AnalyzeRequest(
                userId,
                currentDTOs,
                historicalDTOs
        );

        // Call Python microservice
        AnalyzeResponse response = restTemplate.postForObject(
                aiServiceUrl + "/api/analyze",
                request,
                AnalyzeResponse.class
        );
        if (response == null) {
            throw new RuntimeException("AI service returned null response");
        }

        response.getCategories().forEach((k, v) -> updateTransactionCategory(UUID.fromString(k), v));
        response = changeResponseCategoryToName(response);
        analysisService.saveAnalysis(userId, response);
        return response;
    }

    private TransactionDTO toDTO(UserTransaction t) {
        return new TransactionDTO(
                t.getId(),
                t.getAmount(),
                t.getDescription(),
                t.getDate().toString(),
                t.getCategory(),
                t.getSource().name()
        );
    }

    private void updateTransactionCategory(UUID transactionId, String category) {
       UserTransaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        transaction.setCategory(category);
        transactionRepository.save(transaction);
    }

    private AnalyzeResponse changeResponseCategoryToName(AnalyzeResponse response) {
       Map<String, String> categoryMap = response.getCategories();
       Map<String, String> newMap = new HashMap<>();
       categoryMap.forEach((k,v) -> {
           UserTransaction transaction = transactionRepository.findById(UUID.fromString(k)).orElseThrow(
                   () -> new RuntimeException("Transaction not found")
           );
           newMap.put(transaction.getDescription(), v);

       });
      response.setCategories(newMap);
        return response;
    }

}

