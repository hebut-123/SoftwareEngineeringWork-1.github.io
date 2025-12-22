// TransactionRepository.java
package com.digitalbank.repository;

import com.digitalbank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 根据账户查询交易记录
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionTimeDesc(Long fromAccountId, Long toAccountId);
}