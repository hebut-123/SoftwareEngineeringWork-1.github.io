package com.digitalbank.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 主键ID，自增

    @Column(unique = true, nullable = false, length = 20)
    private String nickname;            // 昵称，唯一，不可为空

    @Column(unique = true, nullable = false, length = 11)
    private String phone;               // 手机号，唯一，不可为空，11位

    @Column(unique = true, nullable = false, length = 18)
    private String idCard;              // 身份证号，唯一，不可为空，18位

    @Column(nullable = false)
    private String password;            // 加密后的密码，不可为空

    private LocalDateTime createTime = LocalDateTime.now();  // 创建时间

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}