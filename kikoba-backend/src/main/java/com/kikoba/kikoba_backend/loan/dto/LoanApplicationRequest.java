package com.kikoba.kikoba_backend.loan.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Loan amount must be at least 1,000")
    private BigDecimal amount;

    @NotNull(message = "Repayment period is required")
    @Min(value = 1, message = "Repayment period must be at least 1 month")
    @Max(value = 60, message = "Repayment period cannot exceed 60 months")
    private Integer repaymentPeriodMonths;

    @Size(max = 255, message = "Purpose cannot exceed 255 characters")
    private String purpose;

    @Size(max = 1000, message = "Collateral description cannot exceed 1000 characters")
    private String collateralDescription;
}