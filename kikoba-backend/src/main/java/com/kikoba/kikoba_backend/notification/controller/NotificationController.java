package com.kikoba.kikoba_backend.notification.controller;

import com.kikoba.kikoba_backend.notification.dto.*;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a notification (Manager only)
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendNotification(request));
    }

    /**
     * Send a bulk notification (Manager only)
     * POST /api/notifications/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationResponse>> sendBulkNotification(
            @Valid @RequestBody BulkNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendBulkNotification(request));
    }

    /**
     * Get my notifications (paginated)
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    /**
     * Get unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    /**
     * Get unread count
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(userId)));
    }

    /**
     * Get summary
     * GET /api/notifications/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryResponse> getSummary(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(notificationService.getSummary(userId));
    }

    /**
     * Get a single notification
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id, userId));
    }

    /**
     * Mark a notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    /**
     * Mark all as read
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal UUID userId) {
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }

    /**
     * Delete a notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Trigger retry of failed notifications (Admin)
     * POST /api/notifications/admin/retry
     */
    @PostMapping("/admin/retry")
    public ResponseEntity<Map<String, Integer>> retryFailed() {
        int count = notificationService.retryFailedNotifications();
        return ResponseEntity.ok(Map.of("retried", count));
    }

    /**
     * Trigger cleanup of expired notifications (Admin)
     * POST /api/notifications/admin/cleanup
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<Map<String, Integer>> cleanupExpired() {
        int count = notificationService.cleanupExpiredNotifications();
        return ResponseEntity.ok(Map.of("expired", count));
    }
}