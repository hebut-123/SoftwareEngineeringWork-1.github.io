package com.digitalbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 10, message = "昵称长度为2-10个字符")
    private String nickname;            // 昵称

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;               // 手机号

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^(\\d{15}|\\d{17}[0-9Xx])$", message = "身份证号格式不正确")
    private String idCard;              // 身份证号

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;            // 密码

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;     // 确认密码
}