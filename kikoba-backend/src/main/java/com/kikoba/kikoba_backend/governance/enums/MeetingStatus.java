package com.kikoba.kikoba_backend.governance.enums;

public enum MeetingStatus {
    SCHEDULED,   // Meeting planned, not started
    ONGOING,     // Meeting in progress
    COMPLETED,   // Meeting ended, minutes locked
    CANCELLED    // Meeting cancelled
}