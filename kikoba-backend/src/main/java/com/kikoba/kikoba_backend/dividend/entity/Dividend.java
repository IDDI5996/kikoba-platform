package com.kikoba.kikoba_backend.dividend.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.dividend.enums.DividendStatus;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dividends", uniqueConstraints = {
        @UniqueConstraint(name = "unique_dividend_cycle", columnNames = {"group_id", "cycle_number"})
}, indexes = {
        @Index(name = "idx_dividends_group", columnList = "group_id"),
        @Index(name = "idx_dividends_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dividend extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber;

    // Financial inputs
    @Column(name = "total_interest_earned", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;

    @Column(name = "total_penalties_collected", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPenaltiesCollected = BigDecimal.ZERO;

    @Column(name = "total_expenses", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    // Calculated profit
    @Column(name = "total_profit", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalProfit;

    // Distribution base
    @Column(name = "total_shares", nullable = false)
    private Integer totalShares;

    @Column(name = "dividend_per_share", nullable = false, precision = 15, scale = 4)
    private BigDecimal dividendPerShare;

    // Lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DividendStatus status = DividendStatus.CALCULATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by")
    private User calculatedBy;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributed_by")
    private User distributedBy;

    @Column(name = "distributed_at")
    private LocalDateTime distributedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}