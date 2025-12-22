// RiskRecordRepository.java
package com.digitalbank.repository;

import com.digitalbank.entity.RiskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RiskRecordRepository extends JpaRepository<RiskRecord, Long> {
    // 查询所有风控记录，按创建时间倒序排列
    List<RiskRecord> findAllByOrderByCreateTimeDesc();

    // 根据状态查询风控记录
    List<RiskRecord> findByStatusOrderByCreateTimeDesc(String status);
}