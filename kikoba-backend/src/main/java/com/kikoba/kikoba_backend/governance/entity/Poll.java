package com.kikoba.kikoba_backend.governance.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.governance.enums.PollStatus;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "polls", indexes = {
        @Index(name = "idx_polls_group", columnList = "group_id"),
        @Index(name = "idx_polls_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Options stored as pipe-separated strings.
     * Example: "Yes|No|Abstain"
     *
     * Using pipe-separated strings instead of JSONB avoids the
     * Jackson 2 vs Jackson 3 dependency conflict in Spring Boot 4.x.
     */
    @Column(name = "options", nullable = false, length = 1000)
    private String options;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PollStatus status = PollStatus.ACTIVE;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @Column(name = "allow_multiple_choices")
    @Builder.Default
    private Boolean allowMultipleChoices = false;

    @Column(name = "closes_at")
    private LocalDateTime closesAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}