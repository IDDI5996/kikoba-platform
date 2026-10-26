package com.kikoba.kikoba_backend.notification.repository;

import com.kikoba.kikoba_backend.notification.entity.Notification;
import com.kikoba.kikoba_backend.notification.enums.NotificationStatus;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.enums.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);

    List<Notification> findByStatusAndRetryCountLessThan(
            NotificationStatus status, Integer maxRetries);

    boolean existsByUserIdAndTypeAndReferenceId(
            UUID userId, NotificationType type, UUID referenceId);

    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.user u
        WHERE n.id = :id
    """)
    Optional<Notification> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT n FROM Notification n
        JOIN FETCH n.user u
        WHERE n.user.id = :userId
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findAllByUserIdWithDetails(@Param("userId") UUID userId);

    @Query("""
        SELECT n FROM Notification n
        WHERE n.expiresAt IS NOT NULL
          AND n.expiresAt < :now
          AND n.status != 'EXPIRED'
    """)
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Count notifications sent to a user in a time window (for rate limiting).
     */
    @Query("""
        SELECT COUNT(n) FROM Notification n
        WHERE n.user.id = :userId
          AND n.channel = :channel
          AND n.createdAt >= :since
    """)
    long countByUserAndChannelSince(
            @Param("userId") UUID userId,
            @Param("channel") NotificationChannel channel,
            @Param("since") LocalDateTime since
    );

    /**
     * Mark all unread notifications as read for a user.
     */
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true, n.readAt = :now, n.status = 'READ'
        WHERE n.user.id = :userId AND n.isRead = false
    """)
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}