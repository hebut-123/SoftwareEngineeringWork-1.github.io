package com.digitalbank.repository;

import com.digitalbank.entity.RiskControlRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RiskControlRecordRepository extends JpaRepository<RiskControlRecord, Long> {
    List<RiskControlRecord> findByRiskLevel(String riskLevel);
    List<RiskControlRecord> findByStatus(String status);

    @Query("SELECT r FROM RiskControlRecord r WHERE r.createTime >= CURRENT_DATE")
    List<RiskControlRecord> findTodayRecords();

    List<RiskControlRecord> findByAccountAccountNumber(String accountNumber);
}