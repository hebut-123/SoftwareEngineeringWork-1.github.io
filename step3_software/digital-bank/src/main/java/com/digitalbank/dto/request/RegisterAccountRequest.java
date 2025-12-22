// RegisterAccountRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterAccountRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;               // 手机号

    @NotBlank(message = "账户类型不能为空")
    private String accountType;         // 账户类型: personal, business

    @NotBlank(message = "分行类型不能为空")
    private String branchType;          // 分行类型: beijing, shanghai, guangzhou, shenzhen

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;            // 密码

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;     // 确认密码
}