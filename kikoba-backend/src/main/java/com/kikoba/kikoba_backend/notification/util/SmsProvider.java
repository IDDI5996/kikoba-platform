package com.kikoba.kikoba_backend.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsProvider {

    @Value("${africastalking.username:}")
    private String username;

    @Value("${africastalking.api-key:}")
    private String apiKey;

    @Value("${notification.sms.enabled:false}")
    private boolean enabled;

    /**
     * Send an SMS to a phone number.
     *
     * In production, this would call Africa's Talking's API:
     * POST https://api.africastalking.com/version1/messaging
     *
     * For now, log to console — replace with actual API call when ready.
     */
    public SmsResult send(String phoneNumber, String message) {
        if (!enabled) {
            log.info("[SMS-DISABLED] Would send to {}: {}", phoneNumber, message);
            return SmsResult.success("SMS-DISABLED-" + System.currentTimeMillis());
        }

        if (username == null || username.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.error("[SMS] Missing Africa's Talking credentials");
            return SmsResult.failure("SMS provider not configured");
        }

        try {
            // TODO: Replace with actual Africa's Talking SDK call
            // AfricasTalking at = new AfricasTalking(username, apiKey);
            // SmsService sms = at.getSmsService();
            // SmsMessage message = new SmsMessage(phoneNumber, message);
            // SmsResponse response = sms.send(message);

            log.info("[SMS] Sent to {}: {}", phoneNumber, message);
            return SmsResult.success("AT-" + System.currentTimeMillis());

        } catch (Exception e) {
            log.error("[SMS] Failed to send to {}: {}", phoneNumber, e.getMessage());
            return SmsResult.failure(e.getMessage());
        }
    }

    /**
     * Send an OTP code via SMS using a standard template.
     */
    public SmsResult sendOtp(String phoneNumber, String otpCode) {
        String message = "KikobaConnect: Your verification code is " + otpCode
                + ". Expires in 5 minutes. Do not share this code.";
        return send(phoneNumber, message);
    }

    // ---- Result wrapper ----
    public record SmsResult(boolean success, String reference, String error) {
        public static SmsResult success(String ref) {
            return new SmsResult(true, ref, null);
        }
        public static SmsResult failure(String error) {
            return new SmsResult(false, null, error);
        }
    }
}