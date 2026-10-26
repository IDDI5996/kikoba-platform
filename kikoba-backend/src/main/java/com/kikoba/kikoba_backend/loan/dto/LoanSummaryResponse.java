package com.kikoba.kikoba_backend.loan.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSummaryResponse {
    private UUID groupId;
    private String groupName;

    private BigDecimal totalDisbursed;
    private BigDecimal totalOutstanding;
    private BigDecimal totalInterestEarned;

    private long totalLoans;
    private long activeLoans;
    private long paidLoans;
    private long pendingLoans;
    private long overdueLoans;

    private BigDecimal portfolioAtRisk;
    private BigDecimal defaultRate;
}