package com.kikoba.kikoba_backend.governance.dto;

import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private UUID id;
    private UUID meetingId;
    private UUID memberId;
    private UUID userId;
    private String memberName;
    private AttendanceStatus status;
    private LocalDateTime rsvpAt;
    private LocalDateTime joinedAt;
    private LocalDateTime firstJoinedAt;
    private LocalDateTime lastHeartbeatAt;
    private LocalDateTime leftAt;
    private Integer durationSeconds;
    private Integer totalDurationSeconds;
    private Integer heartbeatCount;
    private Integer sessionCount;
    private String computedStatusReason;
    private String notes;
}