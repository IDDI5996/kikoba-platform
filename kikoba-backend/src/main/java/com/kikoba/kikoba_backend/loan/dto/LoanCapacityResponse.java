package com.kikoba.kikoba_backend.loan.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCapacityResponse {
    private BigDecimal totalContributions;
    private BigDecimal outstandingLoans;
    private BigDecimal availableFunds;
}