// AccountService.java
package com.digitalbank.service;

import com.digitalbank.dto.request.*;
import com.digitalbank.dto.response.ApiResponse;

public interface AccountService {
    /**
     * 注册账户
     */
    ApiResponse<?> registerAccount(RegisterAccountRequest request);

    /**
     * 查询账户（通过手机号）
     */
    ApiResponse<?> searchAccountByPhone(String phone);

    /**
     * 验证账户
     */
    ApiResponse<?> verifyAccount(VerifyAccountRequest request);

    /**
     * 存款
     */
    ApiResponse<?> deposit(DepositRequest request);

    /**
     * 取款
     */
    ApiResponse<?> withdraw(WithdrawRequest request);

    /**
     * 转账
     */
    ApiResponse<?> transfer(TransferRequest request);

    /**
     * 获取风控数据
     */
    ApiResponse<?> getRiskData();

    /**
     * 验证旧密码
     */
    ApiResponse<?> verifyOldPassword(VerifyOldPasswordRequest request);
    /**
     * 重置密码
     */
    ApiResponse<?> resetPassword(ResetPasswordRequest request);
}