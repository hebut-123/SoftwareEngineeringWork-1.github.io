// ResetPasswordRequest.java
package com.bank.digitalbank.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}