// com.bank.digitalbank.exception.AccountException.java
package com.bank.digitalbank.exception;

public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }

    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}