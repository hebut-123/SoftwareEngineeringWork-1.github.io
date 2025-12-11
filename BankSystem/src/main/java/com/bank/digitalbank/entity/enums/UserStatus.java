package com.bank.digitalbank.entity.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    LOCKED("锁定"),
    FROZEN("冻结"),
    DISABLED("禁用");

    private final String desc;

    UserStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}