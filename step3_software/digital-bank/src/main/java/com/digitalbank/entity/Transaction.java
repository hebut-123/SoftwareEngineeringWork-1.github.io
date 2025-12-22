// Transaction.java - 交易记录实体
package com.digitalbank.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String transactionId;         // 交易流水号

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;          // 转出账户（存款时为null）

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;            // 转入账户（取款时为null）

    @Column(nullable = false)
    private String transactionType;       // 交易类型: DEPOSIT, WITHDRAW, TRANSFER

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;            // 交易金额

    @Column(precision = 15, scale = 2)
    private BigDecimal fromBalanceAfter;  // 转出账户交易后余额

    @Column(precision = 15, scale = 2)
    private BigDecimal toBalanceAfter;    // 转入账户交易后余额

    @Column(nullable = false)
    private LocalDateTime transactionTime = LocalDateTime.now();
}