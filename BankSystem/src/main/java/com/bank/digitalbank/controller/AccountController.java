// com.bank.digitalbank.controller.AccountController.java
package com.bank.digitalbank.controller;

import com.bank.digitalbank.dto.ApiResponse;
import com.bank.digitalbank.dto.account.AccountDTO;
import com.bank.digitalbank.dto.account.CloseAccountRequest;
import com.bank.digitalbank.dto.account.CreateAccountRequest;
import com.bank.digitalbank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取用户所有账户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getUserAccounts() {
        try {
            List<AccountDTO> accounts = accountService.getUserAccounts();
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountDetails(@PathVariable Long accountId) {
        try {
            AccountDTO account = accountService.getAccountById(accountId);
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 通过账号获取账户详情
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            AccountDTO account = accountService.getAccountByNumber(accountNumber);
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建新账户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            AccountDTO account = accountService.createAccount(request);
            return ResponseEntity.ok(ApiResponse.success("账户创建成功", account));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 关闭账户
     */
    @PostMapping("/{accountId}/close")
    public ResponseEntity<ApiResponse<Map<String, String>>> closeAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody CloseAccountRequest request) {
        try {
            accountService.closeAccount(accountId, request);
            return ResponseEntity.ok(ApiResponse.success("账户已成功关闭",
                    Map.of("message", "账户关闭成功", "accountId", accountId.toString())));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检查账户是否存在
     */
    @GetMapping("/exists/{accountNumber}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkAccountExists(@PathVariable String accountNumber) {
        try {
            boolean exists = accountService.existsByAccountNumber(accountNumber);
            return ResponseEntity.ok(ApiResponse.success(Map.of("exists", exists)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("检查账户失败"));
        }
    }
}