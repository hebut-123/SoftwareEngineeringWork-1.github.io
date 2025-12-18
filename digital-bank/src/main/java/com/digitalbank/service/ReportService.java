package com.digitalbank.service;

import com.digitalbank.entity.Transaction;
import com.digitalbank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, Object> getDailyReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionTime().isAfter(startOfDay) &&
                        t.getTransactionTime().isBefore(endOfDay))
                .toList();

        BigDecimal totalDeposit = transactions.stream()
                .filter(t -> "DEPOSIT".equals(t.getTransactionType()) && "SUCCESS".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawal = transactions.stream()
                .filter(t -> "WITHDRAWAL".equals(t.getTransactionType()) && "SUCCESS".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransfer = transactions.stream()
                .filter(t -> "TRANSFER".equals(t.getTransactionType()) && "SUCCESS".equals(t.getStatus()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("date", date.toString());
        report.put("totalTransactions", transactions.size());
        report.put("totalDeposit", totalDeposit);
        report.put("totalWithdrawal", totalWithdrawal);
        report.put("totalTransfer", totalTransfer);
        report.put("transactions", transactions);

        return report;
    }

    public Map<String, Object> getAccountStatement(String accountNumber, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> (t.getFromAccount() != null && accountNumber.equals(t.getFromAccount().getAccountNumber())) ||
                        (t.getToAccount() != null && accountNumber.equals(t.getToAccount().getAccountNumber())))
                .filter(t -> t.getTransactionTime().isAfter(startDateTime) &&
                        t.getTransactionTime().isBefore(endDateTime))
                .sorted((t1, t2) -> t2.getTransactionTime().compareTo(t1.getTransactionTime()))
                .toList();

        Map<String, Object> statement = new HashMap<>();
        statement.put("accountNumber", accountNumber);
        statement.put("startDate", startDate.toString());
        statement.put("endDate", endDate.toString());
        statement.put("transactionCount", transactions.size());
        statement.put("transactions", transactions);

        return statement;
    }
}