package com.kikoba.kikoba_backend.loan.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEligibilityResponse {
    private UUID userId;
    private UUID groupId;

    private BigDecimal totalSavings;
    private BigDecimal maxLoanAmount;
    private BigDecimal savingsMultiplier;

    private boolean eligible;
    private String reason;
    private boolean hasActiveLoan;
}