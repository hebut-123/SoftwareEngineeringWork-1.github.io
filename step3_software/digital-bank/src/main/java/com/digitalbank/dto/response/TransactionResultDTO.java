// TransactionResultDTO.java - 交易结果DTO
package com.digitalbank.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionResultDTO {
    private String transactionId;       // 交易流水号
    private BigDecimal newBalance;      // 交易后余额
    private String message;             // 交易信息
}