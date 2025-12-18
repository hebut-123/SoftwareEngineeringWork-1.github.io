// com.bank.digitalbank.entity.enums.AccountType.java
package com.bank.digitalbank.entity.enums;

public enum AccountType {
    SAVINGS("储蓄账户"),
    CURRENT("活期账户"),
    FIXED_DEPOSIT("定期存款"),
    CREDIT("信用卡账户");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}