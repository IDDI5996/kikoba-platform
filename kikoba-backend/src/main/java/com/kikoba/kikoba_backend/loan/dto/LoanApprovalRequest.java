package com.kikoba.kikoba_backend.loan.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovalRequest {

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    /**
     * Optional: Override interest rate at approval time.
     * If null, uses group's default rate.
     */
    private BigDecimal interestRateOverride;
}