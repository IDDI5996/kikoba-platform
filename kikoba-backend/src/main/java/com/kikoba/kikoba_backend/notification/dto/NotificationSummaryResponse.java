package com.kikoba.kikoba_backend.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryResponse {
    private long totalNotifications;
    private long unreadCount;
    private long sentToday;
    private long failedToday;
}