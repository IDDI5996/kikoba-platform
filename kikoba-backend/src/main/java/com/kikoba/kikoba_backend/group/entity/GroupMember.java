package com.kikoba.kikoba_backend.group.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.group.enums.GroupRole;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_member", columnNames = {"group_id", "user_id"})
}, indexes = {
        @Index(name = "idx_group_members_group_user", columnList = "group_id,user_id"),
        @Index(name = "idx_group_members_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private GroupRole role = GroupRole.MEMBER;

    @Column(name = "total_contributed", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalContributed = BigDecimal.ZERO;

    @Column(name = "total_loan_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalLoanBalance = BigDecimal.ZERO;

    @Column(name = "share_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shareBalance = BigDecimal.ZERO;

    @Column(name = "joined_at")
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}