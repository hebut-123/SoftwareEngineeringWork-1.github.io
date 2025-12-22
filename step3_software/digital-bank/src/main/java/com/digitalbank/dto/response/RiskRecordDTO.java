// RiskRecordDTO.java - 风控记录DTO
package com.digitalbank.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class RiskRecordDTO {
    private Long id;
    private String account;             // 账户号
    private String riskType;            // 风险类型
    private String riskLevel;           // 风险等级
    private BigDecimal amount;          // 触发金额
    private LocalDateTime time;         // 发生时间
    private String status;              // 状态
}
