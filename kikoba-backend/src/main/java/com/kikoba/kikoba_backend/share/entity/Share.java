package com.kikoba.kikoba_backend.share.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.share.enums.ShareTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shares", indexes = {
        @Index(name = "idx_shares_member", columnList = "group_member_id"),
        @Index(name = "idx_shares_group", columnList = "group_id"),
        @Index(name = "idx_shares_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "number_of_shares", nullable = false)
    private Integer numberOfShares;

    @Column(name = "price_per_share", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerShare;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    @Builder.Default
    private ShareTransactionType transactionType = ShareTransactionType.PURCHASE;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;
}