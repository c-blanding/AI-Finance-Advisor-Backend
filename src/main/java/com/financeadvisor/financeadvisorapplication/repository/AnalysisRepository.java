package com.financeadvisor.financeadvisorapplication.repository;

import com.financeadvisor.financeadvisorapplication.entity.AnalysisData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<AnalysisData, Long> {
    Optional<AnalysisData> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<AnalysisData> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
    Optional<List<AnalysisData>> findAllByUserId(UUID userId);
}

