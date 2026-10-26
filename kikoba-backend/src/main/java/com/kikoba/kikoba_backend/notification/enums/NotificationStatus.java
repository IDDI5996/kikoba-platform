package com.kikoba.kikoba_backend.notification.enums;

public enum NotificationStatus {
    PENDING,     // Created, not yet sent
    SENT,        // Sent to channel provider
    DELIVERED,   // Confirmed delivered
    READ,        // User opened/read it
    FAILED,      // Delivery failed
    EXPIRED      // Not read within lifespan
}