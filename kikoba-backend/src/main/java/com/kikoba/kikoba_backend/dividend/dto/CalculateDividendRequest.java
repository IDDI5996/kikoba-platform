package com.kikoba.kikoba_backend.dividend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateDividendRequest {

    @NotNull(message = "Cycle number is required")
    @Min(value = 1, message = "Cycle number must be at least 1")
    private Integer cycleNumber;

    @NotNull(message = "Total expenses is required")
    @DecimalMin(value = "0.00", message = "Expenses cannot be negative")
    @Builder.Default
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}