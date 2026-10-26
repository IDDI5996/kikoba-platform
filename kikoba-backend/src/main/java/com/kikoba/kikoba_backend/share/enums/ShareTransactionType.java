package com.kikoba.kikoba_backend.share.enums;

public enum ShareTransactionType {
    PURCHASE,     // Member buys shares
    TRANSFER_IN,  // Shares received from another member
    TRANSFER_OUT, // Shares transferred to another member
    REDEMPTION,   // Shares redeemed for cash
    ADJUSTMENT    // Manual adjustment by manager
}