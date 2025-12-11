// com.bank.digitalbank.dto.account.AccountDTO.java
package com.bank.digitalbank.dto.account;

import com.bank.digitalbank.entity.enums.AccountStatus;
import com.bank.digitalbank.entity.enums.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String accountName;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime openDate;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}