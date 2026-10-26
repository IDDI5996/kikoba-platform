package com.kikoba.kikoba_backend.notification.service.impl;

import com.kikoba.kikoba_backend.exception.ResourceNotFoundException;
import com.kikoba.kikoba_backend.exception.UnauthorizedException;
import com.kikoba.kikoba_backend.notification.dto.*;
import com.kikoba.kikoba_backend.notification.entity.Notification;
import com.kikoba.kikoba_backend.notification.enums.*;
import com.kikoba.kikoba_backend.notification.mapper.NotificationMapper;
import com.kikoba.kikoba_backend.notification.repository.NotificationPreferenceRepository;
import com.kikoba.kikoba_backend.notification.repository.NotificationRepository;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import com.kikoba.kikoba_backend.notification.util.EmailProvider;
import com.kikoba.kikoba_backend.notification.util.PushProvider;
import com.kikoba.kikoba_backend.notification.util.SmsProvider;
import com.kikoba.kikoba_backend.user.entity.User;
import com.kikoba.kikoba_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_RETRIES = 3;
    private static final int SMS_RATE_LIMIT_PER_HOUR = 5;
    private static final int EMAIL_RATE_LIMIT_PER_HOUR = 10;
    private static final int NOTIFICATION_LIFESPAN_DAYS = 90;

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SmsProvider smsProvider;
    private final EmailProvider emailProvider;
    private final PushProvider pushProvider;
    private final NotificationPersistenceService persistenceService;

    // ============================================
    // SEND NOTIFICATION
    // ============================================
    @Override
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending notification type={} channel={} to user={}",
                request.getType(), request.getChannel(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isCritical(request.getType(), request.getPriority())) {
            if (!isChannelEnabled(user.getId(), request.getType(), request.getChannel())) {
                log.info("Notification skipped: user {} disabled {} via {}",
                        user.getId(), request.getType(), request.getChannel());
                throw new UnauthorizedException(
                        "This notification type is disabled for you on " + request.getChannel());
            }
        }

        if (!checkRateLimit(user.getId(), request.getChannel())) {
            throw new UnauthorizedException(
                    "Rate limit exceeded for " + request.getChannel() + ". Please try later.");
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(request.getType())
                .channel(request.getChannel())
                .priority(request.getPriority())
                .title(request.getTitle())
                .body(request.getBody())
                .actionUrl(request.getActionUrl())
                .metadata(request.getMetadata())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .status(NotificationStatus.PENDING)
                .isRead(false)
                .retryCount(0)
                .expiresAt(LocalDateTime.now().plusDays(NOTIFICATION_LIFESPAN_DAYS))
                .build();

        // ✅ Use the persistence service — this method is called on a DIFFERENT bean
        Notification saved = persistenceService.save(notification);

        // Dispatch inline (still inside the same async thread)
        dispatchNotification(saved);

        return notificationMapper.toResponse(saved);
    }

    // ============================================
    // BULK NOTIFICATION
    // ============================================
    @Override
    @Transactional
    public List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request) {
        log.info("Sending bulk notification to {} users", request.getUserIds().size());

        List<NotificationResponse> responses = new ArrayList<>();

        for (UUID userId : request.getUserIds()) {
            try {
                SendNotificationRequest singleRequest = SendNotificationRequest.builder()
                        .userId(userId)
                        .type(request.getType())
                        .channel(request.getChannel())
                        .priority(request.getPriority())
                        .title(request.getTitle())
                        .body(request.getBody())
                        .actionUrl(request.getActionUrl())
                        .metadata(request.getMetadata())
                        .referenceType(request.getReferenceType())
                        .referenceId(request.getReferenceId())
                        .build();

                responses.add(sendNotification(singleRequest));
            } catch (Exception e) {
                log.warn("Bulk notification failed for user {}: {}", userId, e.getMessage());
            }
        }

        return responses;
    }

    // ============================================
    // CONVENIENCE METHODS
    // ============================================
    @Override
    public void notifyUser(UUID userId, NotificationType type, String title, String body) {
        notifyUser(userId, type, title, body, null);
    }

    @Override
    public void notifyUser(UUID userId, NotificationType type, String title, String body, String actionUrl) {
        try {
            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(userId)
                    .type(type)
                    .channel(NotificationChannel.IN_APP)
                    .priority(NotificationPriority.NORMAL)
                    .title(title)
                    .body(body)
                    .actionUrl(actionUrl)
                    .build();

            sendNotification(request);
        } catch (Exception e) {
            log.error("Failed to notify user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void notifyUserCritical(UUID userId, NotificationType type, String title, String body) {
        List<NotificationChannel> channels = List.of(
                NotificationChannel.IN_APP,
                NotificationChannel.EMAIL,
                NotificationChannel.SMS
        );

        for (NotificationChannel channel : channels) {
            try {
                SendNotificationRequest request = SendNotificationRequest.builder()
                        .userId(userId)
                        .type(type)
                        .channel(channel)
                        .priority(NotificationPriority.URGENT)
                        .title(title)
                        .body(body)
                        .build();

                sendNotification(request);
            } catch (Exception e) {
                log.warn("Critical notification via {} failed: {}", channel, e.getMessage());
            }
        }
    }

    // ============================================
    // ASYNC CONVENIENCE METHODS
    // ============================================
    @Override
    @Async("notificationExecutor")
    public void notifyUserAsync(UUID userId, NotificationType type, String title, String body) {
        notifyUserAsync(userId, type, title, body, null, null, null, NotificationPriority.NORMAL);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyUserAsync(UUID userId, NotificationType type, String title, String body,
                                String actionUrl, UUID referenceId) {
        notifyUserAsync(userId, type, title, body, actionUrl, null, referenceId, NotificationPriority.NORMAL);
    }

    @Override
    @Async("notificationExecutor")
    public void notifyUserAsync(UUID userId, NotificationType type, String title, String body,
                                String actionUrl, String referenceType, UUID referenceId,
                                NotificationPriority priority) {
        try {
            if (referenceId != null
                    && notificationRepository.existsByUserIdAndTypeAndReferenceId(
                    userId, type, referenceId)) {
                log.debug("Skipping duplicate notification: user={}, type={}, ref={}",
                        userId, type, referenceId);
                return;
            }

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .userId(userId)
                    .type(type)
                    .channel(NotificationChannel.IN_APP)
                    .priority(priority)
                    .title(title)
                    .body(body)
                    .actionUrl(actionUrl)
                    .referenceType(referenceType)
                    .referenceId(referenceId)
                    .build();

            sendNotification(request);

            if (priority == NotificationPriority.URGENT) {
                try {
                    SendNotificationRequest smsRequest = SendNotificationRequest.builder()
                            .userId(userId)
                            .type(type)
                            .channel(NotificationChannel.SMS)
                            .priority(priority)
                            .title(title)
                            .body(body)
                            .actionUrl(actionUrl)
                            .referenceType(referenceType)
                            .referenceId(referenceId)
                            .build();
                    sendNotification(smsRequest);
                } catch (Exception e) {
                    log.warn("Urgent SMS to user {} failed: {}", userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Async notification failed for user {}: {}", userId, e.getMessage(), e);
        }
    }

    // ============================================
    // RETRIEVAL
    // ============================================
    @Override
    public NotificationResponse getNotificationById(UUID id, UUID requesterId) {
        Notification notification = notificationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(requesterId)) {
            throw new UnauthorizedException("You cannot view this notification");
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public NotificationSummaryResponse getSummary(UUID userId) {
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        long sentToday = notificationRepository.countByUserAndChannelSince(
                userId, NotificationChannel.IN_APP, oneDayAgo);

        return NotificationSummaryResponse.builder()
                .totalNotifications(unread)
                .unreadCount(unread)
                .sentToday(sentToday)
                .failedToday(0L)
                .build();
    }

    // ============================================
    // ACTIONS
    // ============================================
    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID requesterId) {
        Notification notification = notificationRepository.findByIdWithDetails(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(requesterId)) {
            throw new UnauthorizedException("You cannot modify this notification");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        int count = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID requesterId) {
        Notification notification = notificationRepository.findByIdWithDetails(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(requesterId)) {
            throw new UnauthorizedException("You cannot delete this notification");
        }

        notificationRepository.delete(notification);
    }

    // ============================================
    // SYSTEM JOBS
    // ============================================
    @Override
    @Transactional
    public int retryFailedNotifications() {
        List<Notification> failed = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_RETRIES);

        int retried = 0;
        for (Notification notification : failed) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setFailedAt(null);
            notification.setFailureReason(null);
            dispatchNotification(notification);
            retried++;
        }

        log.info("Retried {} failed notifications", retried);
        return retried;
    }

    @Override
    @Transactional
    public int cleanupExpiredNotifications() {
        List<Notification> expired = notificationRepository.findExpiredNotifications(LocalDateTime.now());

        int count = 0;
        for (Notification notification : expired) {
            notification.setStatus(NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
            count++;
        }

        log.info("Marked {} notifications as expired", count);
        return count;
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================
    private void dispatchNotification(Notification notification) {
        try {
            switch (notification.getChannel()) {
                case IN_APP -> {
                    persistenceService.markDelivered(notification);
                }
                case SMS -> {
                    SmsProvider.SmsResult result = smsProvider.send(
                            notification.getUser().getPhoneNumber(),
                            notification.getTitle() + ": " + notification.getBody());
                    applyResult(notification, result.success(), result.reference(), result.error());
                }
                case EMAIL -> {
                    if (notification.getUser().getEmail() == null) {
                        applyResult(notification, false, null, "User has no email address");
                    } else {
                        EmailProvider.EmailResult result = emailProvider.send(
                                notification.getUser().getEmail(),
                                notification.getTitle(),
                                notification.getBody());
                        applyResult(notification, result.success(), result.reference(), result.error());
                    }
                }
                case PUSH -> {
                    PushProvider.PushResult result = pushProvider.send(
                            notification.getUser().getPushToken(),
                            notification.getTitle(),
                            notification.getBody(),
                            notification.getActionUrl());
                    applyResult(notification, result.success(), result.reference(), result.error());
                }
            }
        } catch (Exception e) {
            log.error("Failed to dispatch notification {}: {}", notification.getId(), e.getMessage(), e);
            persistenceService.markFailed(notification, e.getMessage());
        }
    }

    private void applyResult(Notification notification, boolean success, String reference, String error) {
        if (success) {
            persistenceService.markSent(notification, reference);
        } else {
            persistenceService.markFailed(notification, error);
        }
    }

    private boolean isChannelEnabled(UUID userId, NotificationType type, NotificationChannel channel) {
        return preferenceRepository
                .findByUserIdAndNotificationTypeAndChannel(userId, type.name(), channel.name())
                .map(pref -> pref.getEnabled())
                .orElse(true);
    }

    private boolean isCritical(NotificationType type, NotificationPriority priority) {
        if (priority == NotificationPriority.URGENT) return true;

        return switch (type) {
            case PASSWORD_CHANGED, LOGIN_NEW_DEVICE, MFA_ENABLED, MFA_DISABLED,
                 SECURITY_ALERT, KYC_APPROVED, KYC_REJECTED -> true;
            default -> false;
        };
    }

    private boolean checkRateLimit(UUID userId, NotificationChannel channel) {
        int limit = switch (channel) {
            case SMS -> SMS_RATE_LIMIT_PER_HOUR;
            case EMAIL -> EMAIL_RATE_LIMIT_PER_HOUR;
            default -> Integer.MAX_VALUE;
        };

        if (limit == Integer.MAX_VALUE) return true;

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count = notificationRepository.countByUserAndChannelSince(
                userId, channel, oneHourAgo);

        return count < limit;
    }
}