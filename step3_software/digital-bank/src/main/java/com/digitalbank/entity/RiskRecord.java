// RiskRecord.java - 风控记录实体
package com.digitalbank.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_records")
@Data
public class RiskRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;              // 关联账户

    @Column(nullable = false)
    private String riskType;              // 风险类型: DEPOSIT_OVER_LIMIT, WITHDRAW_OVER_LIMIT, TRANSFER_OVER_LIMIT

    @Column(nullable = false)
    private String riskLevel;             // 风险等级: 高, 中, 低

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;            // 触发金额

    private String description;           // 描述

    @Column(nullable = false)
    private String status = "待处理";      // 状态: 待处理, 处理中, 已处理

    private LocalDateTime createTime = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "待处理";
        }
    }
}
