package com.kikoba.kikoba_backend.dividend.dto;

import com.kikoba.kikoba_backend.dividend.enums.DividendStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private Integer cycleNumber;

    private BigDecimal totalInterestEarned;
    private BigDecimal totalPenaltiesCollected;
    private BigDecimal totalExpenses;
    private BigDecimal totalProfit;

    private Integer totalShares;
    private BigDecimal dividendPerShare;

    private DividendStatus status;

    private UUID calculatedBy;
    private String calculatedByName;
    private LocalDateTime calculatedAt;

    private UUID approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;

    private UUID distributedBy;
    private LocalDateTime distributedAt;

    private String notes;
    private LocalDateTime createdAt;
}