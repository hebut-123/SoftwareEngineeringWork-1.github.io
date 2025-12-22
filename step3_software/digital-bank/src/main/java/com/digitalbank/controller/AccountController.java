// AccountController.java
package com.digitalbank.controller;

import com.digitalbank.dto.request.*;
import com.digitalbank.dto.response.ApiResponse;
import com.digitalbank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/register-account")
    public ApiResponse<?> registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        return accountService.registerAccount(request);
    }

    @GetMapping("/search-account")
    public ApiResponse<?> searchAccountByPhone(@RequestParam String phone) {
        return accountService.searchAccountByPhone(phone);
    }

    @PostMapping("/verify-account")
    public ApiResponse<?> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        return accountService.verifyAccount(request);
    }

    @PostMapping("/deposit")
    public ApiResponse<?> deposit(@Valid @RequestBody DepositRequest request) {
        return accountService.deposit(request);
    }

    @PostMapping("/withdraw")
    public ApiResponse<?> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return accountService.withdraw(request);
    }

    @PostMapping("/transfer")
    public ApiResponse<?> transfer(@Valid @RequestBody TransferRequest request) {
        return accountService.transfer(request);
    }

    @GetMapping("/risk-data")
    public ApiResponse<?> getRiskData() {
        return accountService.getRiskData();
    }
    @PostMapping("/verify-old-password")
    public ApiResponse<?> verifyOldPassword(@Valid @RequestBody VerifyOldPasswordRequest request) {
        return accountService.verifyOldPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return accountService.resetPassword(request);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        // 这里可以清除token等操作
        return ApiResponse.success("退出登录成功");
    }
}