package com.kikoba.kikoba_backend.dividend.enums;

public enum DividendStatus {
    CALCULATED,   // Numbers computed, awaiting approval
    APPROVED,     // Approved, awaiting distribution
    DISTRIBUTED,  // Paid out to members
    CANCELLED     // Cancelled before distribution
}