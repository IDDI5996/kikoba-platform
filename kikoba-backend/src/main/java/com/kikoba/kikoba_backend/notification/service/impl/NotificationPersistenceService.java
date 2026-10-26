package com.kikoba.kikoba_backend.notification.service.impl;

import com.kikoba.kikoba_backend.notification.entity.Notification;
import com.kikoba.kikoba_backend.notification.enums.NotificationStatus;
import com.kikoba.kikoba_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification save(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification persisted: id={}, type={}", saved.getId(), saved.getType());
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification markDelivered(Notification notification) {
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setSentAt(LocalDateTime.now());
        notification.setDeliveredAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification markSent(Notification notification, String reference) {
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        if (notification.getMetadata() == null) {
            notification.setMetadata("providerRef=" + reference);
        } else {
            notification.setMetadata(notification.getMetadata() + "|providerRef=" + reference);
        }
        return notificationRepository.save(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification markFailed(Notification notification, String error) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailedAt(LocalDateTime.now());
        notification.setFailureReason(error);
        return notificationRepository.save(notification);
    }
}