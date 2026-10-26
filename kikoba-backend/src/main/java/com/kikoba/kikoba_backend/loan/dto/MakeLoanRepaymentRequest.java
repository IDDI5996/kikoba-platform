package com.kikoba.kikoba_backend.loan.dto;

import com.kikoba.kikoba_backend.contribution.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MakeLoanRepaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction reference cannot exceed 100 characters")
    private String transactionReference;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}