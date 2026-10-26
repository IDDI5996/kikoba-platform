package com.kikoba.kikoba_backend.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PushProvider {

    @Value("${notification.push.enabled:false}")
    private boolean enabled;

    /**
     * Send a push notification to a device token.
     *
     * In production, use Firebase Admin SDK (FCM) for Android and APNs for iOS.
     */
    public PushResult send(String deviceToken, String title, String body, String actionUrl) {
        if (!enabled) {
            log.info("[PUSH-DISABLED] Would send to token {}: {} - {}",
                    maskToken(deviceToken), title, body);
            return PushResult.success("PUSH-DISABLED-" + System.currentTimeMillis());
        }

        if (deviceToken == null || deviceToken.isBlank()) {
            return PushResult.failure("Device token is empty");
        }

        try {
            // TODO: Replace with actual FCM/APNS SDK call
            // FirebaseMessaging.getInstance().send(message);

            log.info("[PUSH] Sent to token {}: {}", maskToken(deviceToken), title);
            return PushResult.success("FCM-" + System.currentTimeMillis());

        } catch (Exception e) {
            log.error("[PUSH] Failed: {}", e.getMessage());
            return PushResult.failure(e.getMessage());
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    public record PushResult(boolean success, String reference, String error) {
        public static PushResult success(String ref) {
            return new PushResult(true, ref, null);
        }
        public static PushResult failure(String error) {
            return new PushResult(false, null, error);
        }
    }
}