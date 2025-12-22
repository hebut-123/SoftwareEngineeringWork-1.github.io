// DepositRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    @NotBlank(message = "账户号不能为空")
    private String account;             // 账户号

    @NotNull(message = "存款金额不能为空")
    @DecimalMin(value = "0.01", message = "存款金额必须大于0")
    private BigDecimal amount;          // 存款金额
}
