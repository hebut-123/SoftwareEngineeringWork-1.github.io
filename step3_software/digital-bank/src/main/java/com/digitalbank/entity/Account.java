// Account.java - 账户实体
package com.digitalbank.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String accountNumber;        // 账户号码

    @Column(nullable = false)
    private String accountType;          // 账户类型: personal(个人), business(企业)

    @Column(nullable = false)
    private String branchType;           // 分行类型: beijing, shanghai, guangzhou, shenzhen

    @Column(nullable = false)
    private String password;             // 账户密码

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                    // 关联用户

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO; // 账户余额

    private LocalDateTime createTime = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}
