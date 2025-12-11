// com.bank.digitalbank.service.AccountService.java
package com.bank.digitalbank.service;

import com.bank.digitalbank.dto.account.AccountDTO;
import com.bank.digitalbank.dto.account.CloseAccountRequest;
import com.bank.digitalbank.dto.account.CreateAccountRequest;
import com.bank.digitalbank.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountDTO createAccount(CreateAccountRequest request);
    List<AccountDTO> getUserAccounts();
    AccountDTO getAccountById(Long accountId);
    AccountDTO getAccountByNumber(String accountNumber);
    void closeAccount(Long accountId, CloseAccountRequest request);
    void updateAccountBalance(Long accountId, BigDecimal newBalance);
    void updateLastTransactionDate(Long accountId);
    boolean existsByAccountNumber(String accountNumber);
    Account getEntityById(Long accountId);
}