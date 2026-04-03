package com.financeadvisor.financeadvisorapplication.controller;

import com.financeadvisor.financeadvisorapplication.dto.ExchangeTokenRequest;
import com.financeadvisor.financeadvisorapplication.dto.PlaidAccountResponse;
import com.financeadvisor.financeadvisorapplication.service.PlaidService;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {
    private final PlaidService plaidService;

    @GetMapping("/link")
    ResponseEntity<LinkTokenCreateResponse> createLinkToken(@RequestAttribute("userId") UUID userId) throws IOException {

        LinkTokenCreateResponse response = plaidService.createLinkToken(userId);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/exchange")
    ResponseEntity<ItemPublicTokenExchangeResponse> exchangeToken(@RequestAttribute("userId") UUID userId, @RequestBody ExchangeTokenRequest request) throws IOException {
        ItemPublicTokenExchangeResponse response = plaidService.exchangePublicToken(userId, request.getPublicToken());
        return ResponseEntity.ok(response);

    }

    @PostMapping("/sync")
    ResponseEntity<Void> syncTransactions(@RequestAttribute("userId") UUID userId) throws IOException {
        plaidService.syncTransactions(userId);
        return ResponseEntity.status(200).build();

    }

    @GetMapping("/accounts")
    ResponseEntity<List<PlaidAccountResponse>> getAccounts(@RequestAttribute("userId") UUID userId) throws IOException {
        return ResponseEntity.ok(plaidService.getAccounts(userId));
    }
}
