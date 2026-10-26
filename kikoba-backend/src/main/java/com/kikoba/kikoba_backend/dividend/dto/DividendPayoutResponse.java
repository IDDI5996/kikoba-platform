package com.kikoba.kikoba_backend.dividend.dto;

import com.kikoba.kikoba_backend.dividend.enums.PayoutStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendPayoutResponse {
    private UUID id;
    private UUID dividendId;
    private Integer cycleNumber;

    private UUID memberId;
    private UUID userId;
    private String memberName;

    private Integer sharesHeld;
    private BigDecimal amount;
    private PayoutStatus status;

    private LocalDateTime paidAt;
    private String paymentReference;
    private String notes;
    private LocalDateTime createdAt;
}