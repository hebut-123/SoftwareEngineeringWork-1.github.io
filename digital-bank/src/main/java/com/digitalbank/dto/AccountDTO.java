package com.digitalbank.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class AccountDTO {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "账户类型不能为空")
    private String accountType; // SAVINGS, CURRENT, FIXED_DEPOSIT

    private String currency = "CNY";

    @NotNull(message = "初始余额不能为空")
    @PositiveOrZero(message = "初始余额不能为负数")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}