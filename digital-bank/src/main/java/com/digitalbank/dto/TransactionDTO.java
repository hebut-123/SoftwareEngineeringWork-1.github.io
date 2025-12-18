package com.digitalbank.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class TransactionDTO {
    @NotBlank(message = "账户号码不能为空")
    private String accountNumber;

    @NotBlank(message = "目标账户不能为空")
    private String targetAccountNumber;

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "交易类型不能为空")
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
}