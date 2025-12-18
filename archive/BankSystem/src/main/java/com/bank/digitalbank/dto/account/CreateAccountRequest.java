// com.bank.digitalbank.dto.account.CreateAccountRequest.java
package com.bank.digitalbank.dto.account;

import com.bank.digitalbank.entity.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    @NotNull(message = "账户类型不能为空")
    private AccountType type;

    private BigDecimal initialDeposit = BigDecimal.ZERO;

    private String currency = "CNY";
}