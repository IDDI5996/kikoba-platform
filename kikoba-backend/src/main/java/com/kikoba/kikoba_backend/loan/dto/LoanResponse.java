package com.kikoba.kikoba_backend.loan.dto;

import com.kikoba.kikoba_backend.loan.enums.LoanStatus;
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
public class LoanResponse {
    private UUID id;
    private UUID memberId;
    private UUID userId;
    private String memberName;
    private UUID groupId;
    private String groupName;

    private BigDecimal amount;
    private BigDecimal interestRate;
    private BigDecimal totalRepayable;
    private BigDecimal balance;

    private String purpose;
    private LoanStatus status;

    private UUID approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;

    private UUID disbursedBy;
    private LocalDateTime disbursedAt;

    private Integer repaymentPeriodMonths;
    private String repaymentFrequency;
    private LocalDate firstDueDate;

    private String rejectionReason;
    private String collateralDescription;
    private LocalDateTime closedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}