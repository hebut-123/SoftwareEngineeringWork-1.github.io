// VerifyOldPasswordRequest.java
package com.digitalbank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOldPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;               // 手机号

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;         // 旧密码
}
