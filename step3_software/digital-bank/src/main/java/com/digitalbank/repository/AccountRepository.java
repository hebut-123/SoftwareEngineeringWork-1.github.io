// AccountRepository.java
package com.digitalbank.repository;

import com.digitalbank.entity.Account;
import com.digitalbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 根据手机号查询所有账户
    List<Account> findByUserPhone(String phone);

    // 根据账户号查询账户
    Optional<Account> findByAccountNumber(String accountNumber);

    // 判断账户号是否存在
    boolean existsByAccountNumber(String accountNumber);

    // 根据用户ID查询账户
    List<Account> findByUserId(Long userId);
}