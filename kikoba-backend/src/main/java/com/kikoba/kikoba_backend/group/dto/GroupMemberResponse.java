package com.kikoba.kikoba_backend.group.dto;

import com.kikoba.kikoba_backend.group.enums.GroupRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberResponse {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private GroupRole role;
    private BigDecimal totalContributed;
    private BigDecimal totalLoanBalance;
    private BigDecimal shareBalance;
    private LocalDateTime joinedAt;
    private Boolean isActive;
}