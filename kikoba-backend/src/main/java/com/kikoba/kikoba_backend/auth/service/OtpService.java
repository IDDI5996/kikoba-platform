package com.kikoba.kikoba_backend.auth.service;

import com.kikoba.kikoba_backend.auth.enums.OtpPurpose;

public interface OtpService {

    /**
     * Generates a 6-digit OTP, hashes it, saves it, and returns the plaintext
     * code (for immediate SMS/email dispatch — never log or store plaintext).
     */
    String generateAndSave(String recipient, OtpPurpose purpose, String ipAddress);

    /**
     * Verifies an OTP. Increments attempt count on failure.
     * Throws BadRequestException on mismatch/expiry/too many attempts.
     */
    void verify(String recipient, OtpPurpose purpose, String code);

    /**
     * Invalidates all active OTPs for a recipient + purpose.
     */
    void invalidateAll(String recipient, OtpPurpose purpose);

    /**
     * Cleanup job for expired OTPs.
     */
    int cleanupExpired();
}