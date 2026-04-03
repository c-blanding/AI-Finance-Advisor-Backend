package com.financeadvisor.financeadvisorapplication.controller;

import com.financeadvisor.financeadvisorapplication.dto.AnalyzeResponse;
import com.financeadvisor.financeadvisorapplication.service.AIService;
import com.financeadvisor.financeadvisorapplication.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
public class AnalyzeController {
    private final AIService aiService;
    private final AnalysisService analysisService;
    @PostMapping
    public ResponseEntity<AnalyzeResponse> analyze(@RequestAttribute("userId") UUID userId)  {
        try {
            return ResponseEntity.ok(aiService.analyzeSpending(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<AnalyzeResponse> getLatestAnalysis(@RequestAttribute("userId") UUID userId) {
       try {
           return ResponseEntity.ok(analysisService.getLatestAnalysis(userId));
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(null);
       }

    }
}
