package com.kikoba.kikoba_backend.governance.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.governance.enums.MeetingStatus;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings", indexes = {
        @Index(name = "idx_meetings_group", columnList = "group_id"),
        @Index(name = "idx_meetings_status", columnList = "status"),
        @Index(name = "idx_meetings_scheduled", columnList = "scheduled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "agenda", columnDefinition = "TEXT")
    private String agenda;

    @Column(name = "minutes", columnDefinition = "TEXT")
    private String minutes;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "video_meeting_url", length = 500)
    private String videoMeetingUrl;

    @Column(name = "video_meeting_id", length = 100)
    private String videoMeetingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
}