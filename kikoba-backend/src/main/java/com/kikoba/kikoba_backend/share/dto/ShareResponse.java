package com.kikoba.kikoba_backend.share.dto;

import com.kikoba.kikoba_backend.share.enums.ShareTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResponse {
    private UUID id;
    private UUID groupMemberId;
    private UUID userId;
    private String memberName;
    private UUID groupId;
    private String groupName;
    private Integer numberOfShares;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private ShareTransactionType transactionType;
    private String transactionReference;
    private String notes;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}