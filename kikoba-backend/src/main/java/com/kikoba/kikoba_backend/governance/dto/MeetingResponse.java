package com.kikoba.kikoba_backend.governance.dto;

import com.kikoba.kikoba_backend.governance.enums.MeetingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String title;
    private String agenda;
    private String minutes;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private MeetingStatus status;
    private String location;
    private String videoMeetingUrl;
    private String videoMeetingId;
    private UUID createdBy;
    private String createdByName;
    private UUID completedBy;
    private String cancellationReason;
    private long presentCount;
    private long absentCount;
    private LocalDateTime createdAt;
}