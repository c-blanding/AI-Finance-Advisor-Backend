package com.financeadvisor.financeadvisorapplication.service;

import com.financeadvisor.financeadvisorapplication.dto.PlaidAccountResponse;
import com.financeadvisor.financeadvisorapplication.entity.User;
import com.financeadvisor.financeadvisorapplication.entity.UserTransaction;
import com.financeadvisor.financeadvisorapplication.enums.TransactionSource;
import com.financeadvisor.financeadvisorapplication.repository.TransactionRepository;
import com.financeadvisor.financeadvisorapplication.repository.UserRepository;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.plaid.client.model.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaidService {
    private final PlaidApi plaidApi;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public LinkTokenCreateResponse createLinkToken(UUID userId) throws IOException {
        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(new LinkTokenCreateRequestUser()
                        .clientUserId(String.valueOf(userId)))
                .clientName("AI Personal Finance")
                .products(Arrays.asList(Products.TRANSACTIONS))
                .countryCodes(Arrays.asList(CountryCode.US))
                .language("en");

        LinkTokenCreateResponse response = plaidApi
                .linkTokenCreate(request)
                .execute()
                .body();

        return response;
    }

    public ItemPublicTokenExchangeResponse exchangePublicToken(UUID userId, String publicToken) throws IOException {
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                .publicToken(publicToken);
        ItemPublicTokenExchangeResponse response = plaidApi
                .itemPublicTokenExchange(request)
                .execute()
                .body();

        if (response == null) {
            throw new RuntimeException("Plaid returned null response");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPlaidAccessToken(response.getAccessToken());
        userRepository.save(user);
        return response;
    }

    public void syncTransactions(UUID userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Transaction> transactions = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
        TransactionsSyncRequest request = new TransactionsSyncRequest()
                .accessToken(user.getPlaidAccessToken())
                .cursor(user.getPlaidCursor());

        TransactionsSyncResponse response = plaidApi
                    .transactionsSync(request)
                    .execute()
                    .body();


            if (response == null) {
                throw new RuntimeException("Plaid returned null response");
            }
            transactions.addAll(response.getAdded());
            transactions.addAll(response.getModified());
            user.setPlaidCursor(response.getNextCursor());
            hasMore = response.getHasMore();
        }
        List<UserTransaction> newTransactions = transactions.stream().map(t -> mapPlaidToUserTransaction(userId, t)).toList();

        transactionRepository.saveAll(newTransactions);
        userRepository.save(user);

    }

    public List<PlaidAccountResponse> getAccounts(UUID userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPlaidAccessToken() == null) {
            return new ArrayList<>();
        }

        AccountsGetRequest request = new AccountsGetRequest()
                .accessToken(user.getPlaidAccessToken());

        AccountsGetResponse response = plaidApi
                .accountsGet(request)
                .execute()
                .body();

        if (response == null) {
            throw new RuntimeException("Plaid returned null response");
        }

        return response.getAccounts().stream()
                .map(account -> new PlaidAccountResponse(
                        account.getAccountId(),
                        account.getName(),
                        account.getType().getValue(),
                        account.getMask(),
                        account.getBalances().getCurrent()
                ))
                .collect(Collectors.toList());
    }



    public UserTransaction mapPlaidToUserTransaction (UUID userId, Transaction transaction) {

        UserTransaction userTransaction = new UserTransaction();
        String category = (transaction.getCategory() != null && !transaction.getCategory().isEmpty())
                ? transaction.getCategory().getFirst()
                : null;

        userTransaction.setAmount(transaction.getAmount());
        userTransaction.setUserId(userId);
        userTransaction.setCategory(category);
        userTransaction.setDate(transaction.getDate());
        userTransaction.setDescription(transaction.getName());
        userTransaction.setSource(TransactionSource.PLAID);
        return userTransaction;
    }


}
