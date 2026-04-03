package com.financeadvisor.financeadvisorapplication.dto;

import com.financeadvisor.financeadvisorapplication.entity.UserTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {


    UUID id;
    double amount;
    String description;
    String category;
    String source;
    LocalDate date;

    public static TransactionResponse from(UserTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getDescription(),
                t.getCategory(),
                t.getSource().name(),
                t.getDate()
        );
    }
}