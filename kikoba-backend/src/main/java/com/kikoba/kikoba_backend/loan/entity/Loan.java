package com.kikoba.kikoba_backend.loan.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.loan.enums.LoanStatus;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans", indexes = {
        @Index(name = "idx_loans_member", columnList = "group_member_id"),
        @Index(name = "idx_loans_status", columnList = "status"),
        @Index(name = "idx_loans_balance", columnList = "balance"),
        @Index(name = "idx_loans_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "total_repayable", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepayable;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursed_by")
    private User disbursedBy;

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;

    @Column(name = "repayment_period_months", nullable = false)
    private Integer repaymentPeriodMonths;

    @Column(name = "repayment_frequency", length = 20)
    @Builder.Default
    private String repaymentFrequency = "MONTHLY";

    @Column(name = "first_due_date")
    private LocalDate firstDueDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "collateral_description", columnDefinition = "TEXT")
    private String collateralDescription;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}