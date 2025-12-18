package com.digitalbank.controller;

import com.digitalbank.repository.RiskControlRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskControlController {
    @Autowired
    private RiskControlRecordRepository riskControlRecordRepository;

    @GetMapping("/records")
    public ResponseEntity<?> getRiskRecords(
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        try {
            Object records;
            if (riskLevel != null && status != null) {
                records = riskControlRecordRepository.findAll().stream()
                        .filter(r -> riskLevel.equals(r.getRiskLevel()) && status.equals(r.getStatus()))
                        .toList();
            } else if (riskLevel != null) {
                records = riskControlRecordRepository.findByRiskLevel(riskLevel);
            } else if (status != null) {
                records = riskControlRecordRepository.findByStatus(status);
            } else {
                records = riskControlRecordRepository.findAll();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayRiskRecords() {
        try {
            var records = riskControlRecordRepository.findTodayRecords();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", records.size());
            response.put("data", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/process/{recordId}")
    public ResponseEntity<?> processRiskRecord(@PathVariable Long recordId,
                                               @RequestParam String action,
                                               @RequestParam(required = false) String comments) {
        try {
            var record = riskControlRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("风控记录不存在"));

            record.setStatus("PROCESSED");
            record.setAction(action);
            if (comments != null) {
                record.setDescription(record.getDescription() + " | 处理意见：" + comments);
            }
            record.setProcessTime(java.time.LocalDateTime.now());

            riskControlRecordRepository.save(record);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "风控记录处理成功");
            response.put("data", record);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}