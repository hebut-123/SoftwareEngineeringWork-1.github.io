// TransferRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "转出账户不能为空")
    private String fromAccount;         // 转出账户

    @NotBlank(message = "转入账户不能为空")
    private String toAccount;           // 转入账户

    @NotBlank(message = "密码不能为空")
    private String password;            // 转出账户密码

    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额必须大于0")
    private BigDecimal amount;          // 转账金额
}
