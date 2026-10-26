package com.kikoba.kikoba_backend.group.dto;

import com.kikoba.kikoba_backend.group.enums.GroupType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private GroupType type;
    private String description;
    private UUID createdBy;
    private String createdByName;
    private String invitationCode;
    private String meetingFrequency;
    private String meetingDay;
    private LocalTime meetingTime;
    private BigDecimal minContribution;
    private BigDecimal maxContribution;
    private BigDecimal interestRate;
    private Integer cycleDurationMonths;
    private String currency;
    private Boolean isActive;
    private long memberCount;
    private LocalDateTime createdAt;
    private String myRole;
}