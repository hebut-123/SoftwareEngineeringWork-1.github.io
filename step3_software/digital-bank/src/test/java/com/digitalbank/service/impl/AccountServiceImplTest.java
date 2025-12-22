package com.digitalbank.service.impl;

import com.digitalbank.dto.request.TransferRequest;
import com.digitalbank.dto.response.ApiResponse;
import com.digitalbank.entity.Account;
import com.digitalbank.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RiskRecordRepository riskRecordRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private TransferRequest request;
    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        request = new TransferRequest();
        request.setFromAccount("62001");
        request.setToAccount("62002");
        request.setPassword("123456");
        request.setAmount(new BigDecimal("100"));

        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber("62001");
        fromAccount.setPassword("123456"); // 匹配 encodePassword 逻辑
        fromAccount.setBalance(new BigDecimal("1000"));

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("62002");
        toAccount.setBalance(new BigDecimal("500"));
    }

    /**
     * 基本路径测试 - transfer 方法
     */

    @Test
    @DisplayName("路径1：转出账户不存在")
    void transfer_Path1_FromAccountNotFound() {
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.empty());

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("FROM_ACCOUNT_NOT_FOUND", response.getCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("路径2：转出账户密码错误")
    void transfer_Path2_PasswordError() {
        request.setPassword("wrong_pwd");
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("PASSWORD_ERROR", response.getCode());
    }

    @Test
    @DisplayName("路径3：转入账户不存在")
    void transfer_Path3_ToAccountNotFound() {
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("62002")).thenReturn(Optional.empty());

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("TO_ACCOUNT_NOT_FOUND", response.getCode());
    }

    @Test
    @DisplayName("路径4：不能转账给自己")
    void transfer_Path4_SelfTransfer() {
        request.setToAccount("62001");
        // 模拟同一个对象或相同ID
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("SELF_TRANSFER", response.getCode());
    }

    @Test
    @DisplayName("路径5：余额不足")
    void transfer_Path5_InsufficientBalance() {
        request.setAmount(new BigDecimal("10000")); // 超过余额
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("62002")).thenReturn(Optional.of(toAccount));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("INSUFFICIENT_BALANCE", response.getCode());
    }

    @Test
    @DisplayName("路径6：成功转账且触发风控（金额 >= 20万）")
    void transfer_Path6_SuccessWithRisk() {
        BigDecimal largeAmount = new BigDecimal("250000");
        request.setAmount(largeAmount);
        fromAccount.setBalance(new BigDecimal("300000"));

        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("62002")).thenReturn(Optional.of(toAccount));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("SUCCESS", response.getCode());
        // 验证余额扣减
        assertEquals(new BigDecimal("50000"), fromAccount.getBalance());
        // 验证风控记录被创建
        verify(riskRecordRepository, times(1)).save(any());
        // 验证交易记录被创建
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("路径7：成功转账但不触发风控（金额 < 20万）")
    void transfer_Path7_SuccessNoRisk() {
        when(accountRepository.findByAccountNumber("62001")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("62002")).thenReturn(Optional.of(toAccount));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("SUCCESS", response.getCode());
        // 验证风控记录未被创建
        verify(riskRecordRepository, never()).save(any());
        verify(accountRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("路径8：数据库操作异常触发 catch")
    void transfer_Path8_Exception() {
        when(accountRepository.findByAccountNumber(anyString())).thenThrow(new RuntimeException("DB Error"));

        ApiResponse<?> response = accountService.transfer(request);

        assertEquals("TRANSFER_ERROR", response.getCode());
        assertTrue(response.getMessage().contains("DB Error"));
    }
}