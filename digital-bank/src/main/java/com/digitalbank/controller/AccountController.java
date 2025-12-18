package com.digitalbank.controller;

import com.digitalbank.entity.Account;
import com.digitalbank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody Account account,
                                           @RequestParam String userId) {
        try {
            Account createdAccount = accountService.createAccount(account, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "账户创建成功");
            response.put("data", createdAccount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "账户创建失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        try {
            Account account = accountService.getAccountByNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("账户不存在"));
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        try {
            BigDecimal balance = accountService.getBalance(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}