package com.financeadvisor.financeadvisorapplication.repository;

import com.financeadvisor.financeadvisorapplication.entity.UserTransaction;
import com.financeadvisor.financeadvisorapplication.enums.TransactionSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<UserTransaction, UUID> {
    List<UserTransaction> findByUserId(UUID userId);
    List<UserTransaction> findAllByUserIdOrderByDateDesc(UUID userId);

    List<UserTransaction> findByUserIdOrderByDateDesc(UUID userId);
    List<UserTransaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
    List<UserTransaction> findByUserIdAndDate(UUID userId, LocalDate transactionDate);
    Optional<UserTransaction> findByUserIdAndId(UUID userId, UUID transactionId);
    List<UserTransaction> findByUserIdAndSource(UUID userId, TransactionSource source);
    List<UserTransaction> findByUserIdAndDateAndSource(UUID userId, LocalDate transactionDate, TransactionSource source);
}
