package com.bank.digitalbank.entity;

import com.bank.digitalbank.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的 users 表
 */
@Data
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_phone", columnList = "phone")
        })
@DynamicInsert // 插入时只赋值非空字段
@DynamicUpdate // 更新时只更新修改的字段
public class User {

    /**
     * 用户ID，自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID'")
    private Long id;

    /**
     * 用户名（登录名），唯一且不可为空
     */
    @Column(unique = true, nullable = false, length = 50, columnDefinition = "VARCHAR(50) NOT NULL COMMENT '用户名（登录名）'")
    private String username;

    /**
     * 真实姓名
     */
    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '真实姓名'")
    private String fullname;

    /**
     * 邮箱，唯一且不可为空
     */
    @Column(unique = true, nullable = false, length = 100, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '用户邮箱'")
    private String email;

    /**
     * 手机号，唯一且不可为空
     */
    @Column(unique = true, nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '手机号'")
    private String phone;

    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '加密密码'")
    private String password;

    /**
     * 用户状态（激活/未激活/锁定/冻结等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '用户状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定，FROZEN-冻结'")
    private UserStatus status = UserStatus.INACTIVE;

    /**
     * 账户是否启用（Spring Security 兼容）
     */
    @Column(nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户是否启用'")
    private boolean enabled = true;

    /**
     * 账户是否未过期（Spring Security 兼容）
     */
    @Column(name = "account_non_expired", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户是否未过期'")
    private boolean accountNonExpired = true;

    /**
     * 凭证是否未过期（Spring Security 兼容）
     */
    @Column(name = "credentials_non_expired", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '凭证是否未过期'")
    private boolean credentialsNonExpired = true;

    /**
     * 账户是否未锁定（Spring Security 兼容）
     */
    @Column(name = "account_non_locked", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账户是否未锁定'")
    private boolean accountNonLocked = true;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedAt;

    /**
     * 持久化前的操作（设置创建和更新时间）
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 更新前的操作（更新时间）
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}