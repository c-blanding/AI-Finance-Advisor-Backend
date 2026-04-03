package com.financeadvisor.financeadvisorapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String categories;         // stored as JSON string

    @Column(columnDefinition = "TEXT")
    private String overspending;       // stored as JSON string

    @Column(columnDefinition = "TEXT")
    private String recommendations;    // stored as JSON string

    @Column(columnDefinition = "TEXT")
    private String predictions;        // stored as JSON string

    @CreationTimestamp
    private LocalDateTime createdAt;
}
