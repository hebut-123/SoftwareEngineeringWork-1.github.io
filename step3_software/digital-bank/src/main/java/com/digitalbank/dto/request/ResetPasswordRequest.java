// ResetPasswordRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;               // 手机号

    @NotBlank(message = "新密码不能为空")
    private String newPassword;         // 新密码

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;     // 确认密码
}