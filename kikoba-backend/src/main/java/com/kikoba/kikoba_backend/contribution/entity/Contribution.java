package com.kikoba.kikoba_backend.contribution.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.contribution.enums.ContributionStatus;
import com.kikoba.kikoba_backend.contribution.enums.PaymentMethod;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_contributions_member", columnList = "group_member_id"),
        @Index(name = "idx_contributions_period", columnList = "contribution_period"),
        @Index(name = "idx_contributions_status", columnList = "status"),
        @Index(name = "idx_contributions_created_at", columnList = "created_at"),
        @Index(name = "idx_contributions_reference", columnList = "transaction_reference")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ContributionStatus status = ContributionStatus.PENDING;

    @Column(name = "contribution_period")
    private LocalDate contributionPeriod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by")
    private java.util.UUID confirmedBy;
}