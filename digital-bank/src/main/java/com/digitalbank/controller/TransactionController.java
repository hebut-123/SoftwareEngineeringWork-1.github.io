package com.digitalbank.controller;

import com.digitalbank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestParam String accountNumber,
                                     @RequestParam BigDecimal amount,
                                     @RequestParam(required = false) String description) {
        try {
            if (description == null) {
                description = "存款";
            }
            var transaction = transactionService.deposit(accountNumber, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "存款成功");
            response.put("data", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "存款失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestParam String accountNumber,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String description) {
        try {
            if (description == null) {
                description = "取款";
            }
            var transaction = transactionService.withdraw(accountNumber, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "取款成功");
            response.put("data", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取款失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestParam String fromAccountNumber,
                                      @RequestParam String toAccountNumber,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String description) {
        try {
            if (description == null) {
                description = "转账";
            }
            var transaction = transactionService.transfer(fromAccountNumber, toAccountNumber, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "转账成功");
            response.put("data", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "转账失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}