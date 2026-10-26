package com.kikoba.kikoba_backend.loan.enums;

public enum RepaymentStatus {
    PENDING,        // Scheduled but not yet paid
    COMPLETED,      // Paid
    OVERDUE,        // Past due date, not paid
    PARTIAL,        // Partially paid
    WAIVED          // Penalty waived (optional)
}