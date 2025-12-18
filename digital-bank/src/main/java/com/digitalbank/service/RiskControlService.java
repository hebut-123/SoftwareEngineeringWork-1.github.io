package com.digitalbank.service;

import com.digitalbank.entity.RiskControlRecord;
import com.digitalbank.entity.Transaction;
import com.digitalbank.repository.AccountRepository;
import com.digitalbank.repository.RiskControlRecordRepository;
import com.digitalbank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RiskControlService {
    @Autowired
    private RiskControlRecordRepository riskControlRecordRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("20000");

    public void checkDeposit(Transaction transaction) {
        // 大额存款检查
        if (transaction.getAmount().compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            createRiskRecord(transaction, "LARGE_AMOUNT", "MEDIUM",
                    "大额存款：" + transaction.getAmount());
        }
    }

    public void checkWithdrawal(String accountNumber, BigDecimal amount) {
        // 大额取款检查
        if (amount.compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            createRiskRecord(null, "LARGE_AMOUNT", "HIGH",
                    "大额取款：" + amount + "，账户：" + accountNumber);
        }

        // 检查当日取款总额
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<Transaction> todayWithdrawals = transactionRepository.findAll().stream()
                .filter(t -> "WITHDRAWAL".equals(t.getTransactionType()))
                .filter(t -> t.getFromAccount() != null && accountNumber.equals(t.getFromAccount().getAccountNumber()))
                .filter(t -> t.getTransactionTime().isAfter(todayStart))
                .toList();

        BigDecimal dailyTotal = todayWithdrawals.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (dailyTotal.add(amount).compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new RuntimeException("超过当日取款限额");
        }
    }

    public void checkTransfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        // 大额转账检查
        if (amount.compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            createRiskRecord(null, "LARGE_AMOUNT", "HIGH",
                    "大额转账：" + amount + "，从" + fromAccountNumber + "到" + toAccountNumber);
        }

        // 检查是否为可疑账户（简化版）
        if (isSuspiciousAccount(toAccountNumber)) {
            createRiskRecord(null, "SUSPICIOUS", "HIGH",
                    "向可疑账户转账，账户：" + toAccountNumber);
        }
    }

    private boolean isSuspiciousAccount(String accountNumber) {
        // 这里可以连接外部风控系统或使用规则引擎
        // 简化实现：检查是否在可疑账户列表中
        List<String> suspiciousAccounts = List.of("可疑账户1", "可疑账户2");
        return suspiciousAccounts.contains(accountNumber);
    }

    private void createRiskRecord(Transaction transaction, String riskType,
                                  String riskLevel, String description) {
        RiskControlRecord record = new RiskControlRecord();
        record.setRecordId("RISK" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        record.setTransaction(transaction);
        record.setRiskType(riskType);
        record.setRiskLevel(riskLevel);
        record.setDescription(description);

        riskControlRecordRepository.save(record);

        // 这里可以发送告警通知，记录日志等
        System.out.println("风控告警：" + description);
    }
}