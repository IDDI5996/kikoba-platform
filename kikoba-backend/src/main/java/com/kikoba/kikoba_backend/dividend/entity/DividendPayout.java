package com.kikoba.kikoba_backend.dividend.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.dividend.enums.PayoutStatus;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dividend_payouts", uniqueConstraints = {
        @UniqueConstraint(name = "unique_dividend_member", columnNames = {"dividend_id", "group_member_id"})
}, indexes = {
        @Index(name = "idx_payouts_dividend", columnList = "dividend_id"),
        @Index(name = "idx_payouts_member", columnList = "group_member_id"),
        @Index(name = "idx_payouts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendPayout extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dividend_id", nullable = false)
    private Dividend dividend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Column(name = "shares_held", nullable = false)
    private Integer sharesHeld;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}