// VerifyAccountRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyAccountRequest {
    @NotBlank(message = "账户号不能为空")
    private String account;             // 账户号

    @NotBlank(message = "密码不能为空")
    private String password;            // 密码
}