// AccountInfoDTO.java - 账户信息DTO
package com.digitalbank.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountInfoDTO {
    private String accountNumber;       // 账户号码
    private String accountType;         // 账户类型
    private String branch;              // 分行名称
    private BigDecimal balance;         // 账户余额
}