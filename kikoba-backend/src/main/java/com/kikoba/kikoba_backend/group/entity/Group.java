package com.kikoba.kikoba_backend.group.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.group.enums.GroupType;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_groups_type", columnList = "type"),
        @Index(name = "idx_groups_invitation_code", columnList = "invitation_code"),
        @Index(name = "idx_groups_created_by", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private GroupType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "invitation_code", unique = true, length = 20)
    private String invitationCode;

    @Column(name = "meeting_frequency", length = 20)
    private String meetingFrequency;

    @Column(name = "meeting_day", length = 20)
    private String meetingDay;

    @Column(name = "meeting_time")
    private LocalTime meetingTime;

    @Column(name = "min_contribution", precision = 15, scale = 2)
    private BigDecimal minContribution;

    @Column(name = "max_contribution", precision = 15, scale = 2)
    private BigDecimal maxContribution;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "cycle_duration_months")
    private Integer cycleDurationMonths;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "TZS";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "share_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal sharePrice = new BigDecimal("100.00");   // Default: 100 TZS per share
}