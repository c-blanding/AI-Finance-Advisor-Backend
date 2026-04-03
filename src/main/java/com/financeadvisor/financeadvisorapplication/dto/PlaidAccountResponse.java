package com.financeadvisor.financeadvisorapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaidAccountResponse {
    private String id;
    private String name;
    private String type;
    private String mask;
    private Double balance;
}