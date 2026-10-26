package com.kikoba.kikoba_backend.loan.enums;

public enum LoanStatus {
    PENDING,        // Applied, awaiting approval
    APPROVED,       // Approved, awaiting disbursement
    REJECTED,       // Rejected by manager
    ACTIVE,         // Disbursed, being repaid
    PAID,           // Fully repaid
    DEFAULTED,      // Overdue beyond threshold
    CANCELLED       // Cancelled before approval
}