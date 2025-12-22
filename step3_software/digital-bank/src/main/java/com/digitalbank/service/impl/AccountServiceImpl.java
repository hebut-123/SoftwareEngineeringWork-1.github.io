// AccountServiceImpl.java
package com.digitalbank.service.impl;

import com.digitalbank.dto.request.*;
import com.digitalbank.dto.response.*;
import com.digitalbank.entity.*;
import com.digitalbank.repository.*;
import com.digitalbank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final BigDecimal DEPOSIT_RISK_LIMIT = new BigDecimal("1000000");    // 存款风控阈值：100万
    private static final BigDecimal WITHDRAW_RISK_LIMIT = new BigDecimal("200000");    // 取款风控阈值：20万
    private static final BigDecimal TRANSFER_RISK_LIMIT = new BigDecimal("200000");    // 转账风控阈值：20万

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RiskRecordRepository riskRecordRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional
    public ApiResponse<?> registerAccount(RegisterAccountRequest request) {
        try {
            // 1. 验证用户是否存在
            User user = userRepository.findByPhone(request.getPhone())
                    .orElse(null);
            if (user == null) {
                return ApiResponse.error("USER_NOT_FOUND", "用户不存在，请先注册用户");
            }

            // 2. 密码一致性验证
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("PASSWORDS_NOT_MATCH", "两次密码不一致");
            }

            // 3. 生成账户号（62开头，19位）
            String accountNumber = generateAccountNumber();

            // 4. 创建账户
            Account account = new Account();
            account.setAccountNumber(accountNumber);
            account.setAccountType(request.getAccountType());
            account.setBranchType(request.getBranchType());
            account.setPassword(encodePassword(request.getPassword()));
            account.setUser(user);
            account.setBalance(BigDecimal.ZERO);
            account.setCreateTime(LocalDateTime.now());

            account = accountRepository.save(account);

            // 5. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType", getAccountTypeName(request.getAccountType()));
            data.put("branch", getBranchName(request.getBranchType()));
            data.put("balance", account.getBalance());

            return ApiResponse.success(data);

        } catch (Exception e) {
            // 方法1：重新抛出RuntimeException
            throw new RuntimeException("业务处理失败", e);
        }
    }

    @Override
    public ApiResponse<?> searchAccountByPhone(String phone) {
        try {
            // 1. 查询用户的所有账户
            List<Account> accounts = accountRepository.findByUserPhone(phone);

            if (accounts.isEmpty()) {
                return ApiResponse.error("NO_ACCOUNTS", "该手机号下没有账户");
            }

            // 2. 转换为DTO
            List<AccountInfoDTO> accountDTOs = accounts.stream()
                    .map(account -> {
                        AccountInfoDTO dto = new AccountInfoDTO();
                        dto.setAccountNumber(account.getAccountNumber());
                        dto.setAccountType(getAccountTypeName(account.getAccountType()));
                        dto.setBranch(getBranchName(account.getBranchType()));
                        dto.setBalance(account.getBalance());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // 3. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("accounts", accountDTOs);

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("SEARCH_ERROR", "查询账户失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> verifyAccount(VerifyAccountRequest request) {
        try {
            // 1. 查询账户
            Account account = accountRepository.findByAccountNumber(request.getAccount())
                    .orElse(null);
            if (account == null) {
                return ApiResponse.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }

            // 2. 验证密码
            if (!account.getPassword().equals(encodePassword(request.getPassword()))) {
                return ApiResponse.error("PASSWORD_ERROR", "密码错误");
            }

            // 3. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType", getAccountTypeName(account.getAccountType()));
            data.put("balance", account.getBalance());

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("VERIFY_ERROR", "验证账户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> deposit(DepositRequest request) {
        try {
            // 1. 查询账户
            Account account = accountRepository.findByAccountNumber(request.getAccount())
                    .orElse(null);
            if (account == null) {
                return ApiResponse.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }

            // 2. 存款
            BigDecimal newBalance = account.getBalance().add(request.getAmount());
            account.setBalance(newBalance);
            accountRepository.save(account);

            // 3. 检查是否需要风控标记
            if (request.getAmount().compareTo(DEPOSIT_RISK_LIMIT) >= 0) {
                createRiskRecord(account, "DEPOSIT_OVER_LIMIT", "高",
                        request.getAmount(), "存款金额超过100万风控阈值");
            }

            // 4. 创建交易记录
            Transaction transaction = createTransaction(null, account,
                    "DEPOSIT", request.getAmount(), null, newBalance);
            transactionRepository.save(transaction);

            // 5. 构建响应数据
            TransactionResultDTO resultDTO = new TransactionResultDTO();
            resultDTO.setTransactionId(transaction.getTransactionId());
            resultDTO.setNewBalance(newBalance);
            resultDTO.setMessage("存款成功");

            Map<String, Object> data = new HashMap<>();
            data.put("transaction", resultDTO);

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("DEPOSIT_ERROR", "存款失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> withdraw(WithdrawRequest request) {
        try {
            // 1. 查询账户
            Account account = accountRepository.findByAccountNumber(request.getAccount())
                    .orElse(null);
            if (account == null) {
                return ApiResponse.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }

            // 2. 检查余额是否充足
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                return ApiResponse.error("INSUFFICIENT_BALANCE", "账户余额不足");
            }

            // 3. 取款
            BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
            account.setBalance(newBalance);
            accountRepository.save(account);

            // 4. 检查是否需要风控标记
            if (request.getAmount().compareTo(WITHDRAW_RISK_LIMIT) >= 0) {
                createRiskRecord(account, "WITHDRAW_OVER_LIMIT", "高",
                        request.getAmount(), "取款金额超过20万风控阈值");
            }

            // 5. 创建交易记录
            Transaction transaction = createTransaction(account, null,
                    "WITHDRAW", request.getAmount(), newBalance, null);
            transactionRepository.save(transaction);

            // 6. 构建响应数据
            TransactionResultDTO resultDTO = new TransactionResultDTO();
            resultDTO.setTransactionId(transaction.getTransactionId());
            resultDTO.setNewBalance(newBalance);
            resultDTO.setMessage("取款成功");

            Map<String, Object> data = new HashMap<>();
            data.put("transaction", resultDTO);

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("WITHDRAW_ERROR", "取款失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> transfer(TransferRequest request) {
        try {
            // 1. 查询转出账户
            Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccount())
                    .orElse(null);
            if (fromAccount == null) {
                return ApiResponse.error("FROM_ACCOUNT_NOT_FOUND", "转出账户不存在");
            }

            // 2. 验证转出账户密码
            if (!fromAccount.getPassword().equals(encodePassword(request.getPassword()))) {
                return ApiResponse.error("PASSWORD_ERROR", "转出账户密码错误");
            }

            // 3. 查询转入账户
            Account toAccount = accountRepository.findByAccountNumber(request.getToAccount())
                    .orElse(null);
            if (toAccount == null) {
                return ApiResponse.error("TO_ACCOUNT_NOT_FOUND", "转入账户不存在");
            }

            // 4. 检查不能转账给自己
            if (fromAccount.getId().equals(toAccount.getId())) {
                return ApiResponse.error("SELF_TRANSFER", "不能转账给自己");
            }

            // 5. 检查余额是否充足
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                return ApiResponse.error("INSUFFICIENT_BALANCE", "转出账户余额不足");
            }

            // 6. 转账
            BigDecimal fromNewBalance = fromAccount.getBalance().subtract(request.getAmount());
            BigDecimal toNewBalance = toAccount.getBalance().add(request.getAmount());

            fromAccount.setBalance(fromNewBalance);
            toAccount.setBalance(toNewBalance);

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // 7. 检查是否需要风控标记
            if (request.getAmount().compareTo(TRANSFER_RISK_LIMIT) >= 0) {
                createRiskRecord(fromAccount, "TRANSFER_OVER_LIMIT", "高",
                        request.getAmount(), "转账金额超过20万风控阈值");
            }

            // 8. 创建交易记录
            Transaction transaction = createTransaction(fromAccount, toAccount,
                    "TRANSFER", request.getAmount(), fromNewBalance, toNewBalance);
            transactionRepository.save(transaction);

            // 9. 构建响应数据
            TransactionResultDTO resultDTO = new TransactionResultDTO();
            resultDTO.setTransactionId(transaction.getTransactionId());
            resultDTO.setNewBalance(fromNewBalance);
            resultDTO.setMessage("转账成功");

            Map<String, Object> data = new HashMap<>();
            data.put("transaction", resultDTO);

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("TRANSFER_ERROR", "转账失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getRiskData() {
        try {
            // 1. 查询所有风控记录
            List<RiskRecord> riskRecords = riskRecordRepository.findAllByOrderByCreateTimeDesc();

            // 2. 转换为DTO
            List<RiskRecordDTO> riskDTOs = riskRecords.stream()
                    .map(record -> {
                        RiskRecordDTO dto = new RiskRecordDTO();
                        dto.setId(record.getId());
                        dto.setAccount(record.getAccount().getAccountNumber());
                        dto.setRiskType(getRiskTypeName(record.getRiskType()));
                        dto.setRiskLevel(record.getRiskLevel());
                        dto.setAmount(record.getAmount());
                        dto.setTime(record.getCreateTime());
                        dto.setStatus(record.getStatus());
                        return dto;
                    })
                    .collect(Collectors.toList());

            // 3. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("riskRecords", riskDTOs);

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("RISK_DATA_ERROR", "获取风控数据失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> verifyOldPassword(VerifyOldPasswordRequest request) {
        try {
            // 1. 后端验证手机号格式
            if (request.getPhone() == null || request.getPhone().length() != 11) {
                return ApiResponse.error("VALIDATION_ERROR", "手机号格式不正确");
            }

            // 2. 后端验证旧密码不能为空
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                return ApiResponse.error("VALIDATION_ERROR", "旧密码不能为空");
            }

            // 3. 查询用户
            User user = userRepository.findByPhone(request.getPhone())
                    .orElse(null);
            if (user == null) {
                return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
            }

            // 4. 验证旧密码
            if (!user.getPassword().equals(encodePassword(request.getOldPassword()))) {
                return ApiResponse.error("PASSWORD_ERROR", "旧密码错误");
            }

            // 5. 验证成功，返回用户信息
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("nickname", user.getNickname());

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("VERIFY_ERROR", "验证失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> resetPassword(ResetPasswordRequest request) {
        try {
            // 1. 后端验证手机号格式
            if (request.getPhone() == null || request.getPhone().length() != 11) {
                return ApiResponse.error("VALIDATION_ERROR", "手机号格式不正确");
            }

            // 2. 后端验证密码一致性
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.error("VALIDATION_ERROR", "两次密码不一致");
            }

            // 4. 查询用户
            User user = userRepository.findByPhone(request.getPhone())
                    .orElse(null);
            if (user == null) {
                return ApiResponse.error("USER_NOT_FOUND", "用户不存在");
            }

            // 5. 更新用户密码
            user.setPassword(encodePassword(request.getNewPassword()));
            userRepository.save(user);


            // 7. 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("resetTime", LocalDateTime.now());

            return ApiResponse.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("RESET_PASSWORD_ERROR", "重置密码失败: " + e.getMessage());
        }
    }
    // =============== 工具方法 ===============

    // 生成账户号（62开头，19位）
    private String generateAccountNumber() {
        // 62开头 + 时间戳 + 随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "62" + timestamp.substring(timestamp.length() - 13) + random;
    }

    // 密码加密
    private String encodePassword(String password) {
        // TODO: 使用BCryptPasswordEncoder加密
        // return new BCryptPasswordEncoder().encode(password);
        return password; // 暂时返回原密码
    }

    // 创建风控记录
    private void createRiskRecord(Account account, String riskType, String riskLevel,
                                  BigDecimal amount, String description) {
        RiskRecord riskRecord = new RiskRecord();
        riskRecord.setAccount(account);
        riskRecord.setRiskType(riskType);
        riskRecord.setRiskLevel(riskLevel);
        riskRecord.setAmount(amount);
        riskRecord.setDescription(description);
        riskRecord.setStatus("待处理");
        riskRecord.setCreateTime(LocalDateTime.now());

        riskRecordRepository.save(riskRecord);
    }

    // 创建交易记录
    private Transaction createTransaction(Account fromAccount, Account toAccount,
                                          String transactionType, BigDecimal amount,
                                          BigDecimal fromBalanceAfter, BigDecimal toBalanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TR" + System.currentTimeMillis());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setFromBalanceAfter(fromBalanceAfter);
        transaction.setToBalanceAfter(toBalanceAfter);
        transaction.setTransactionTime(LocalDateTime.now());

        return transaction;
    }

    // 获取账户类型名称
    private String getAccountTypeName(String typeCode) {
        switch (typeCode) {
            case "personal": return "个人账户";
            case "business": return "企业账户";
            default: return typeCode;
        }
    }

    // 获取分行名称
    private String getBranchName(String branchCode) {
        switch (branchCode) {
            case "beijing": return "北京分行";
            case "shanghai": return "上海分行";
            case "guangzhou": return "广州分行";
            case "shenzhen": return "深圳分行";
            default: return branchCode;
        }
    }

    // 获取风险类型名称
    private String getRiskTypeName(String riskType) {
        switch (riskType) {
            case "DEPOSIT_OVER_LIMIT": return "存款超限";
            case "WITHDRAW_OVER_LIMIT": return "取款超限";
            case "TRANSFER_OVER_LIMIT": return "转账超限";
            default: return riskType;
        }
    }
}