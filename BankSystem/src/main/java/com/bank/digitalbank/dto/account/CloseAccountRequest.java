// com.bank.digitalbank.dto.account.CloseAccountRequest.java
package com.bank.digitalbank.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseAccountRequest {
    @NotBlank(message = "关闭原因不能为空")
    private String reason;

    private String remarks;
}