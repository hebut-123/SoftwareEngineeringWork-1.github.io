package com.digitalbank.service;

import com.digitalbank.entity.Account;
import com.digitalbank.entity.Transaction;
import com.digitalbank.repository.AccountRepository;
import com.digitalbank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RiskControlService riskControlService;

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        try {
            // 执行存款
            accountService.deposit(accountNumber, amount);

            // 创建交易记录
            Transaction transaction = new Transaction();
            transaction.setTransactionId(generateTransactionId());
            transaction.setTransactionType("DEPOSIT");
            transaction.setToAccount(accountRepository.findByAccountNumber(accountNumber).orElse(null));
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setStatus("SUCCESS");

            // 风控检查
            riskControlService.checkDeposit(transaction);

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            Transaction failedTransaction = new Transaction();
            failedTransaction.setTransactionId(generateTransactionId());
            failedTransaction.setTransactionType("DEPOSIT");
            failedTransaction.setAmount(amount);
            failedTransaction.setDescription(description);
            failedTransaction.setStatus("FAILED");
            failedTransaction.setFailureReason(e.getMessage());
            transactionRepository.save(failedTransaction);

            throw e;
        }
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        try {
            // 风控检查
            riskControlService.checkWithdrawal(accountNumber, amount);

            // 执行取款
            accountService.withdraw(accountNumber, amount);

            // 创建交易记录
            Transaction transaction = new Transaction();
            transaction.setTransactionId(generateTransactionId());
            transaction.setTransactionType("WITHDRAWAL");
            transaction.setFromAccount(accountRepository.findByAccountNumber(accountNumber).orElse(null));
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setStatus("SUCCESS");

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            Transaction failedTransaction = new Transaction();
            failedTransaction.setTransactionId(generateTransactionId());
            failedTransaction.setTransactionType("WITHDRAWAL");
            failedTransaction.setAmount(amount);
            failedTransaction.setDescription(description);
            failedTransaction.setStatus("FAILED");
            failedTransaction.setFailureReason(e.getMessage());
            transactionRepository.save(failedTransaction);

            throw e;
        }
    }

    @Transactional
    public Transaction transfer(String fromAccountNumber, String toAccountNumber,
                                BigDecimal amount, String description) {
        try {
            // 风控检查
            riskControlService.checkTransfer(fromAccountNumber, toAccountNumber, amount);

            // 执行转账
            accountService.withdraw(fromAccountNumber, amount);
            accountService.deposit(toAccountNumber, amount);

            // 创建交易记录
            Transaction transaction = new Transaction();
            transaction.setTransactionId(generateTransactionId());
            transaction.setTransactionType("TRANSFER");
            transaction.setFromAccount(accountRepository.findByAccountNumber(fromAccountNumber).orElse(null));
            transaction.setToAccount(accountRepository.findByAccountNumber(toAccountNumber).orElse(null));
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setStatus("SUCCESS");

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            Transaction failedTransaction = new Transaction();
            failedTransaction.setTransactionId(generateTransactionId());
            failedTransaction.setTransactionType("TRANSFER");
            failedTransaction.setAmount(amount);
            failedTransaction.setDescription(description);
            failedTransaction.setStatus("FAILED");
            failedTransaction.setFailureReason(e.getMessage());
            transactionRepository.save(failedTransaction);

            throw e;
        }
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}