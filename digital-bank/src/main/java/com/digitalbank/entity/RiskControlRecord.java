package com.digitalbank.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_control_records")
@Data
public class RiskControlRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recordId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(nullable = false)
    private String riskType; // LARGE_AMOUNT, FREQUENT, SUSPICIOUS

    private String riskLevel; // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, PROCESSED, IGNORED

    private String action; // BLOCK, ALLOW, REVIEW

    private String description;

    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime processTime;
}