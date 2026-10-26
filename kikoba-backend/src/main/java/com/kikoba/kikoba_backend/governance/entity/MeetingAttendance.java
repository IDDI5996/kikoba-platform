package com.kikoba.kikoba_backend.governance.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.governance.enums.AttendanceStatus;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_attendance", uniqueConstraints = {
        @UniqueConstraint(name = "unique_meeting_member", columnNames = {"meeting_id", "group_member_id"})
}, indexes = {
        @Index(name = "idx_attendance_meeting", columnList = "meeting_id"),
        @Index(name = "idx_attendance_member", columnList = "group_member_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingAttendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember groupMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "rsvp_at")
    private LocalDateTime rsvpAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "first_joined_at")
    private LocalDateTime firstJoinedAt;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "current_session_started_at")
    private LocalDateTime currentSessionStartedAt;

    @Column(name = "total_duration_seconds")
    @Builder.Default
    private Integer totalDurationSeconds = 0;

    @Column(name = "heartbeat_count")
    @Builder.Default
    private Integer heartbeatCount = 0;

    @Column(name = "session_count")
    @Builder.Default
    private Integer sessionCount = 0;

    @Column(name = "computed_status_reason", length = 500)
    private String computedStatusReason;
}