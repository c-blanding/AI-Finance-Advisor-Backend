package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.dto.TransactionRequest;
import com.financeadvisor.financeadvisorapplication.entity.UserTransaction;
import com.financeadvisor.financeadvisorapplication.enums.TransactionSource;
import com.financeadvisor.financeadvisorapplication.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {


    private final TransactionRepository transactionRepository;


    public List<UserTransaction> getUserTransactions(UUID userId) {
          return transactionRepository.findAllByUserIdOrderByDateDesc(userId);
    }
   public List<UserTransaction> getUserTransactionsByUserIdAndSource(UUID userId, TransactionSource source) {
        return transactionRepository.findByUserIdAndSource(userId, source);
   }
   public List<UserTransaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate, UUID userId) {
        return transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
   }
   public List<UserTransaction> getTransactionsByDate(LocalDate date, UUID userId) {
        return transactionRepository.findByUserIdAndDate(userId, date);
   }


   public UserTransaction createTransaction(UUID userId, TransactionRequest request) {
        UserTransaction userTransaction = new UserTransaction();
        userTransaction.setUserId(userId);
        userTransaction.setDate(request.getDate());
        userTransaction.setAmount(request.getAmount());
        userTransaction.setDescription(request.getDescription());
        userTransaction.setCategory(request.getCategory());
        userTransaction.setSource(TransactionSource.MANUAL);
        return transactionRepository.save(userTransaction);
   }
   public void deleteTransactions(UUID userId, UUID transactionId) {
        UserTransaction userTransaction = transactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow(() -> new RuntimeException("Transaction Or User not found"));
        transactionRepository.delete(userTransaction);
   }


}
