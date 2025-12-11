// com.bank.digitalbank.service.impl.AccountServiceImpl.java
package com.bank.digitalbank.service.impl;

import com.bank.digitalbank.dto.account.AccountDTO;
import com.bank.digitalbank.dto.account.CloseAccountRequest;
import com.bank.digitalbank.dto.account.CreateAccountRequest;
import com.bank.digitalbank.entity.Account;
import com.bank.digitalbank.entity.User;
import com.bank.digitalbank.entity.enums.AccountStatus;
import com.bank.digitalbank.entity.enums.AccountType;
import com.bank.digitalbank.repository.AccountRepository;
import com.bank.digitalbank.repository.UserRepository;
import com.bank.digitalbank.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountDTO createAccount(CreateAccountRequest request) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();

        // 生成唯一的账户号码
        String accountNumber = generateAccountNumber();

        // 检查账户号码是否唯一
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }

        // 创建账户
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUser(currentUser);
        account.setAccountName(request.getAccountName());
        account.setType(request.getType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit() != null ?
                request.getInitialDeposit() : BigDecimal.ZERO);
        account.setCurrency(request.getCurrency());

        if (account.getType() == AccountType.SAVINGS) {
            account.setInterestRate(new BigDecimal("0.0035")); // 0.35%
        } else if (account.getType() == AccountType.CURRENT) {
            account.setInterestRate(new BigDecimal("0.0010")); // 0.10%
        } else if (account.getType() == AccountType.FIXED_DEPOSIT) {
            account.setInterestRate(new BigDecimal("0.0150")); // 1.50%
        }

        Account savedAccount = accountRepository.save(account);

        log.info("账户创建成功: 用户={}, 账号={}, 类型={}, 初始余额={}",
                currentUser.getUsername(), accountNumber,
                request.getType(), request.getInitialDeposit());

        return convertToDTO(savedAccount);
    }

    @Override
    public List<AccountDTO> getUserAccounts() {
        User currentUser = getCurrentUser();
        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());

        return accounts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        // 验证账户所有者
        verifyAccountOwnership(account);

        return convertToDTO(account);
    }

    @Override
    public AccountDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        return convertToDTO(account);
    }

    @Override
    @Transactional
    public void closeAccount(Long accountId, CloseAccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        // 验证账户所有者
        verifyAccountOwnership(account);

        // 检查账户状态
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("账户已关闭");
        }

        // 检查账户余额
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("账户仍有余额，请先清空余额再关闭账户");
        }

        // 更新账户状态
        account.setStatus(AccountStatus.CLOSED);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);

        log.info("账户已关闭: 账号={}, 原因={}, 备注={}",
                account.getAccountNumber(), request.getReason(), request.getRemarks());
    }

    @Override
    @Transactional
    public void updateAccountBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateLastTransactionDate(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        account.setLastTransactionDate(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public Account getEntityById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        verifyAccountOwnership(account);

        return account;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private void verifyAccountOwnership(Account account) {
        User currentUser = getCurrentUser();

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("无权访问此账户");
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        // 生成16位账户号码：622202 + 10位随机数字
        StringBuilder sb = new StringBuilder("622202");

        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountName(account.getAccountName());
        dto.setType(account.getType());
        dto.setStatus(account.getStatus());
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setOpenDate(account.getOpenDate());
        dto.setLastTransactionDate(account.getLastTransactionDate());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}