// com.bank.digitalbank.repository.AccountRepository.java
package com.bank.digitalbank.repository;

import com.bank.digitalbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);
    boolean existsByAccountNumber(String accountNumber);
}