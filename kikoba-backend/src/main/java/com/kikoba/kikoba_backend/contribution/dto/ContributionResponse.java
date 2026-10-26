package com.kikoba.kikoba_backend.contribution.dto;

import com.kikoba.kikoba_backend.contribution.enums.ContributionStatus;
import com.kikoba.kikoba_backend.contribution.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionResponse {
    private UUID id;
    private UUID memberId;
    private UUID userId;
    private String memberName;
    private UUID groupId;
    private String groupName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private ContributionStatus status;
    private String transactionReference;
    private LocalDate contributionPeriod;
    private String notes;
    private String receiptUrl;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}