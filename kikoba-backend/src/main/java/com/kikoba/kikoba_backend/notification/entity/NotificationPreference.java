package com.kikoba.kikoba_backend.notification.entity;

import com.kikoba.kikoba_backend.common.BaseEntity;
import com.kikoba.kikoba_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_type_channel",
                columnNames = {"user_id", "notification_type", "channel"})
}, indexes = {
        @Index(name = "idx_prefs_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;
}