package com.kikoba.kikoba_backend.share.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareSummaryResponse {
    private UUID userId;
    private UUID groupId;
    private String groupName;
    private Integer totalShares;
    private BigDecimal currentSharePrice;
    private BigDecimal totalValue;
    private long totalTransactions;
}