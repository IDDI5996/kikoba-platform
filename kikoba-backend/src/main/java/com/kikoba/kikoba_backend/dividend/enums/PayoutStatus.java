package com.kikoba.kikoba_backend.dividend.enums;

public enum PayoutStatus {
    PENDING,      // Awaiting payment
    PAID,         // Successfully paid
    FAILED,       // Payment failed
    CANCELLED     // Cancelled
}