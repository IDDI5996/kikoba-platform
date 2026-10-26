package com.kikoba.kikoba_backend.loan.dto;

import com.kikoba.kikoba_backend.contribution.enums.PaymentMethod;
import com.kikoba.kikoba_backend.loan.enums.RepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentResponse {
    private UUID id;
    private UUID loanId;
    private Integer installmentNumber;
    private LocalDate dueDate;

    private BigDecimal amountDue;
    private BigDecimal principalDue;
    private BigDecimal interestDue;

    private BigDecimal amountPaid;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal penaltyPaid;

    private RepaymentStatus status;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private LocalDate paymentDate;
    private LocalDateTime confirmedAt;
    private String notes;
}