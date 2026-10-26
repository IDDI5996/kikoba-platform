package com.kikoba.kikoba_backend.governance.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.governance.enums.AnnouncementCategory;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "announcements", indexes = {
        @Index(name = "idx_announcements_group", columnList = "group_id"),
        @Index(name = "idx_announcements_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @Builder.Default
    private AnnouncementCategory category = AnnouncementCategory.GENERAL;

    @Column(name = "pinned")
    @Builder.Default
    private Boolean pinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}