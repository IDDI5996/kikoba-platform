package com.kikoba.kikoba_backend.contribution.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionSummaryResponse {
    private UUID groupId;
    private String groupName;
    private BigDecimal totalContributed;
    private long totalTransactions;
    private BigDecimal currentPeriodTotal;
    private LocalDate currentPeriod;
}