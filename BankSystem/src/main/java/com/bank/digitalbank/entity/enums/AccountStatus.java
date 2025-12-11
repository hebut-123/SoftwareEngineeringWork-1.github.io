// com.bank.digitalbank.entity.enums.AccountStatus.java
package com.bank.digitalbank.entity.enums;

public enum AccountStatus {
    ACTIVE("活跃"),
    INACTIVE("非活跃"),
    CLOSED("已关闭"),
    FROZEN("冻结"),
    PENDING("待激活");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}